package com.zdzhai.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.zdzhai.apicommon.common.CookieConstant;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.constant.OrderInfoConstant;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.dto.ApiOrderTokenRequest;
import com.zdzhai.apicommon.model.dto.UserInterfaceInfoUpdateRequest;
import com.zdzhai.apicommon.model.entity.ApiOrder;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.service.InnerInterfaceInfoService;
import com.zdzhai.apicommon.service.InnerUserInterfaceInfoService;
import com.zdzhai.apicommon.utils.*;
import com.zdzhai.order.constant.OrderConstant;
import com.zdzhai.order.mapper.ApiOrderMapper;
import com.zdzhai.order.model.dto.order.ApiOrderAddRequest;
import com.zdzhai.order.model.dto.order.ApiOrderCancelRequest;
import com.zdzhai.order.model.dto.order.ApiOrderStatusInfoDto;
import com.zdzhai.order.model.vo.ApiOrderStatusVO;
import com.zdzhai.order.model.vo.OrderSnVO;
import com.zdzhai.order.service.ApiOrderService;
import com.zdzhai.order.utils.RedisIdWorker;
import org.apache.dubbo.config.annotation.DubboReference;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 62618
 * @description 针对表【api_order】的数据库操作Service实现
 * @createDate 2023-12-17 16:48:17
 */
@Service
public class ApiOrderServiceImpl extends ServiceImpl<ApiOrderMapper, ApiOrder>
        implements ApiOrderService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ApiOrderMapper apiOrderMapper;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @Resource
    private ApplicationContext APPLICATION_CONTEXT;

    @Resource
    private RedisIdWorker redisIdWorker;

    /**
     * 用户信息的redisKEY
     */
    private final String API_USER_ID = "api:user:";

    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    public void generateToken(HttpServletRequest request,
                              HttpServletResponse response) {
        User user = this.getLoginUser(request);
        Long userId = user.getId();
        if (null == userId) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        //防重令牌
        String token = IdUtil.simpleUUID();
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId, token, 30, TimeUnit.MINUTES);
        Cookie cookie = new Cookie(CookieConstant.orderToken, token);
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.orderTokenExpireTime);
        response.addCookie(cookie);
    }

    /**
     * 生成订单
     *
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
        Cookie[] cookies = request.getCookies();
        User loginUser = this.getLoginUser(request);
        if (null == loginUser) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2、健壮性校验
        String totalAmount = "", charging = "";
        try {
            totalAmount = AmountUtils.changeY2F(apiOrderAddRequest.getTotalAmount());
            charging = AmountUtils.changeY2F(apiOrderAddRequest.getCharging());
        } catch (Exception e) {
            log.error("金钱转换异常！");
        }
        Long orderNum = apiOrderAddRequest.getOrderNum();
        Long interfaceId = apiOrderAddRequest.getInterfaceId();
        if ("".equals(totalAmount) || null == orderNum || "".equals(charging) || null == interfaceId) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3、查询接口信息
        InterfaceInfo interfaceInfoById = innerInterfaceInfoService.getById(interfaceId);
        if (interfaceInfoById == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //存整数
        Long temp = orderNum  * Long.parseLong(charging) ;
        if (!temp.equals(Long.parseLong(totalAmount))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        //4、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        String token = null;
        for (Cookie cookie : cookies) {
            if (CookieConstant.orderToken.equals(cookie.getName())) {
                token = cookie.getValue();
            }
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId()),
                token);
        if (result == 0L) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交太快了，请重新提交");
        }

        //5、使用雪花算法思想生成订单id，并保存订单
        String orderSn = redisIdWorker.nextOrderSn("api:order");
        ApiOrder apiOrder = new ApiOrder();
        apiOrder.setTotalAmount(Long.parseLong(totalAmount));
        apiOrder.setOrderSn(orderSn);
        apiOrder.setOrderNum(orderNum);
        apiOrder.setStatus(OrderConstant.toBePaid);
        apiOrder.setInterfaceId(interfaceId);
        apiOrder.setUserId(loginUser.getId());
        apiOrder.setCharging(Integer.parseInt(charging));
        try {
            apiOrderMapper.insert(apiOrder);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单保存失败");
        }

        //todo 是否应该在订单完成后再做接口数量的增加操作 6、更新剩余可调用接口数量
        UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest = new UserInterfaceInfoUpdateRequest();
        userInterfaceInfoUpdateRequest.setOrderNum(orderNum);
        userInterfaceInfoUpdateRequest.setInterfaceInfoId(interfaceId);
        userInterfaceInfoUpdateRequest.setUserId(loginUser.getId());
        // 参数校验
        User user = this.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean updateUserInterfaceInfo = innerUserInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoUpdateRequest);
        if (!updateUserInterfaceInfo) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户接口数更新失败");
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
            System.out.println("时间:" + System.currentTimeMillis() + ";延迟下单消息生产者发送一条信息，内容:{" + apiOrder.getOrderSn() + "}");
        } catch (Exception e) {
            System.out.println(("订单接收异常"));
            e.printStackTrace();
        }

        //8、构建返回给前端页面的数据
        OrderSnVO orderSnVO = new OrderSnVO();
        BeanUtils.copyProperties(apiOrder, orderSnVO);
        DateTime date = DateUtil.date();
        orderSnVO.setCharging(charging);
        orderSnVO.setTotalAmount(totalAmount);
        orderSnVO.setCreateTime(date);
        orderSnVO.setExpirationTime(DateUtil.offset(date, DateField.MINUTE, 30));
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
        this.update(new UpdateWrapper<ApiOrder>().eq("orderSn", orderSn).set("status", 2));
        //减少接口余量
        ApiOrderTokenRequest apiOrderTokenRequest = new ApiOrderTokenRequest();
        Cookie[] cookies = request.getCookies();
        apiOrderTokenRequest.setRequestCookie(cookies);
        User loginUser = this.getLoginUser(request);
        if (null == loginUser) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest = new UserInterfaceInfoUpdateRequest();
        userInterfaceInfoUpdateRequest.setOrderNum(orderNum * -1);
        userInterfaceInfoUpdateRequest.setInterfaceInfoId(apiOrderCancelRequest.getInterfaceId());
        userInterfaceInfoUpdateRequest.setUserId(loginUser.getId());
        // 参数校验
        User user = this.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean updateUserInterfaceInfo = innerUserInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoUpdateRequest);
        if (!updateUserInterfaceInfo) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消订单失败");
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
     * 获取当前登录用户的status订单信息
     *
     * @param statusInfoDto
     * @param request
     * @return
     */
    @Override
    public Page<ApiOrderStatusVO> getCurrentOrderInfo(ApiOrderStatusInfoDto statusInfoDto, HttpServletRequest request) {
        Long userId = statusInfoDto.getUserId();
        //前端筛选即可
        Integer status = statusInfoDto.getStatus();
        if (null == userId) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = statusInfoDto.getCurrent();
        // 限制爬虫
        long size = statusInfoDto.getPageSize();
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ApiOrderStatusVO> apiOrderStatusVO = apiOrderMapper.getCurrentOrderInfo(new Page<>(current, size), userId, status);
        List<ApiOrderStatusVO> records = apiOrderStatusVO.getRecords();
        List<ApiOrderStatusVO> collect = records.stream().map(record -> {
            Integer status1 = record.getStatus();
            if (status1 == 0) {
                Date date = record.getCreateTime();
                record.setExpirationTime(DateUtil.offset(date, DateField.MINUTE, 30));
            }
            try {
                record.setCharging(AmountUtils.changeF2Y(record.getCharging()));
                record.setTotalAmount(AmountUtils.changeF2Y(record.getTotalAmount()));
            } catch (Exception e) {
                log.error("getCurrentOrderInfo: 金钱转换错误！");
            }
            return record;
        }).collect(Collectors.toList());
        apiOrderStatusVO.setRecords(collect);
        return apiOrderStatusVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        String authorization = null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (CookieConstant.headAuthorization.equals(name)) {
                authorization = cookie.getValue();
            }
        }
        //token 信息有问题则抛异常
        if (authorization == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2、验证token是否合法，判断当前登录用户和token中的用户是否相同
        TokenUtils tokenUtils = new TokenUtils();
        boolean verifyToken = tokenUtils.verifyToken(authorization);
        if (!verifyToken) {
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        // 3、验证token是否过期
        boolean verifyTime = tokenUtils.verifyTokenTime(authorization);
        if (!verifyTime) {
            //过期了需要重新登录
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录状态过期，请重新登录");
        }
        JWT jwt = JWTUtil.parseToken(authorization);
        String sId = (String) jwt.getPayload("id");
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(API_USER_ID + sId);
        User user = new User();
        if (userMap != null && userMap.size() != 0) {
            user = BeanUtil.mapToBean(userMap, User.class, true, CopyOptions.create()
                    .setIgnoreNullValue(true)
                    .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        }
        return user;
    }
}




