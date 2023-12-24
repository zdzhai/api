package com.zdzhai.project.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.zdzhai.apicommon.common.CookieConstant;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.constant.OrderInfoConstant;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.constant.OrderConstant;
import com.zdzhai.project.mapper.ApiOrderMapper;
import com.zdzhai.project.model.dto.order.ApiOrderAddRequest;
import com.zdzhai.project.model.dto.order.ApiOrderCancelRequest;
import com.zdzhai.project.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import com.zdzhai.project.model.entity.ApiOrder;
import com.zdzhai.project.model.vo.OrderSnVO;
import com.zdzhai.project.service.ApiOrderService;
import com.zdzhai.project.service.InterfaceInfoService;
import com.zdzhai.project.service.UserInterfaceInfoService;
import com.zdzhai.project.service.UserService;
import com.zdzhai.project.utils.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
* @author 62618
* @description 针对表【api_order】的数据库操作Service实现
* @createDate 2023-12-17 16:48:17
*/
@Service
public class ApiOrderServiceImpl extends ServiceImpl<ApiOrderMapper, ApiOrder>
    implements ApiOrderService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ApiOrderMapper apiOrderMapper;

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private ApplicationContext APPLICATION_CONTEXT;

    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     * @param request
     * @param response
     * @return
     */
    @Override
    public void generateToken(HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getLoginUser(request,response);
        Long userId = user.getId();
        if (null == userId){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        //防重令牌
        String token = IdUtil.simpleUUID();
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId,token,30, TimeUnit.MINUTES);
        Cookie cookie = new Cookie(CookieConstant.orderToken,token);
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.orderTokenExpireTime);
        response.addCookie(cookie);
    }

    /**
     * 生成订单
     * @param apiOrderAddRequest
     * @param request
     * @param response
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSnVO generateOrderSn(@RequestBody ApiOrderAddRequest apiOrderAddRequest,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
            throws ExecutionException, InterruptedException {
        //1、远程获取当前登录用户
        User loginUser = userService.getLoginUser(request,response);
        if (null == loginUser){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2、健壮性校验
        Long totalAmount = apiOrderAddRequest.getTotalAmount();
        Long orderNum = apiOrderAddRequest.getOrderNum();
        Integer charging = apiOrderAddRequest.getCharging();
        Long interfaceId = apiOrderAddRequest.getInterfaceId();
        if ( null==totalAmount || null == orderNum || null ==charging || null==interfaceId){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3、查询接口信息
        InterfaceInfo interfaceInfoById = interfaceInfoService.getById(interfaceId);
        if (interfaceInfoById == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //存整数
        Long temp = orderNum * charging;
        if (!temp.equals(totalAmount)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        //4、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        Cookie[] cookies = request.getCookies();
        String token = null;
        for (Cookie cookie : cookies) {
            if (CookieConstant.orderToken.equals(cookie.getName())){
                token = cookie.getValue();
            }
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId()),
                token);
        if (result == 0L) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"提交太快了，请重新提交");
        }

        //5、todo 使用雪花算法生成订单id，并保存订单
        String orderSn = generateOrderSn(loginUser.getId().toString());
        ApiOrder apiOrder = new ApiOrder();
        apiOrder.setTotalAmount(totalAmount);
        apiOrder.setOrderSn(orderSn);
        apiOrder.setOrderNum(orderNum);
        apiOrder.setStatus(OrderConstant.toBePaid);
        apiOrder.setInterfaceId(interfaceId);
        apiOrder.setUserId(loginUser.getId());
        apiOrder.setCharging(charging);
        try {
            apiOrderMapper.insert(apiOrder);
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"订单保存失败");
        }

        //todo 是否应该在订单完成后再做接口数量的增加操作 6、更新剩余可调用接口数量
        UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest = new UserInterfaceInfoUpdateRequest();
        userInterfaceInfoUpdateRequest.setOrderNum(orderNum);
        userInterfaceInfoUpdateRequest.setInterfaceInfoId(interfaceId);
        userInterfaceInfoUpdateRequest.setUserId(loginUser.getId());
        boolean updateUserInterfaceInfo = userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoUpdateRequest, request, response);
        if (!updateUserInterfaceInfo) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"库存更新失败");
        }
        //7、全部完成后，向mq延时队列发送延迟订单消息，且30分钟过期
        //等待异步任务完成
        try {
            DefaultMQProducer orderInfoProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("MQProducer");
            Gson gson = new Gson();
            Message msg = new Message();
            msg.setTopic(OrderInfoConstant.TOPIC_ORDERS);
            msg.setBody(gson.toJson(apiOrder).getBytes());
            msg.setDelayTimeLevel(16);
            RocketMQUtil.asyncSendMsg(orderInfoProducer, msg);
            System.out.println("时间:"+ System.currentTimeMillis()+";延迟下单消息生产者发送一条信息，内容:{"+apiOrder.getOrderSn()+"}");
        } catch (Exception e) {
            System.out.println(("订单接收异常"));
            e.printStackTrace();
        }

        //8、构建返回给前端页面的数据
        OrderSnVO orderSnVO = new OrderSnVO();
        BeanUtils.copyProperties(apiOrder,orderSnVO);
        DateTime date = DateUtil.date();
        orderSnVO.setCreateTime(date);
        orderSnVO.setExpirationTime(DateUtil.offset(date, DateField.MINUTE,30));
        orderSnVO.setName(interfaceInfoById.getName());
        orderSnVO.setDescription(interfaceInfoById.getDescription());
        return orderSnVO;
    }

    @Override
    @Transactional
    public String cancelOrderSn(ApiOrderCancelRequest apiOrderCancelRequest,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        Long orderNum = apiOrderCancelRequest.getOrderNum();
        String orderSn = apiOrderCancelRequest.getOrderSn();
        //订单已经被取消的情况
        ApiOrder orderSn1 = this.getOne(new QueryWrapper<ApiOrder>().eq("orderSn", orderSn));
        if (orderSn1.getStatus() == 2) {
            return "取消订单成功";
        }
        //更新订单表状态
        this.update(new UpdateWrapper<ApiOrder>().eq("orderSn", orderSn).set("status",2));
        //减少接口余量
        User loginUser = userService.getLoginUser(request,response);
        if (null == loginUser){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest = new UserInterfaceInfoUpdateRequest();
        userInterfaceInfoUpdateRequest.setOrderNum(orderNum * -1);
        userInterfaceInfoUpdateRequest.setInterfaceInfoId(apiOrderCancelRequest.getInterfaceId());
        userInterfaceInfoUpdateRequest.setUserId(loginUser.getId());
        boolean updateUserInterfaceInfo = userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoUpdateRequest, request, response);
        if (!updateUserInterfaceInfo) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"取消订单失败");
        }
        return "取消订单成功";
    }

    @Override
    public int updateApiOrderStatusByOrderSn(String orderSn, int status) {
        return apiOrderMapper.updateApiOrderStatusByOrderSn(orderSn, status);
    }

    @Override
    public ApiOrder getApiOrderByOrderSn(String orderSn) {
        return apiOrderMapper.getApiOrderByOrderSn(orderSn);
    }

    /**
     * 生成订单号
     * @return
     */
    private String generateOrderSn(String userId) {
        //todo 按照黑马点评的订单号生成方法,高并发下有可能造成订单号重复
        //todo 分布式数据库中也有可能造成订单号重复
        String timeId = IdWorker.getTimeId();
        String substring = timeId.substring(0, timeId.length() - 15);
        return substring + RandomUtil.randomNumbers(10) + userId.toString();
    }

}




