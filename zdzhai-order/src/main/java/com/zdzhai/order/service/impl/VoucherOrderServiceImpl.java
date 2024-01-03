package com.zdzhai.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.order.mapper.VoucherOrderMapper;
import com.zdzhai.order.model.entity.SeckillVoucher;
import com.zdzhai.order.model.entity.VoucherOrder;
import com.zdzhai.order.service.ApiOrderService;
import com.zdzhai.order.service.VoucherOrderService;
import com.zdzhai.order.utils.RedisIdWorker;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author 62618
 * @description 针对表【voucher_order】的数据库操作Service实现
 * @createDate 2023-12-26 19:37:17
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements VoucherOrderService {

    @Resource
    private SeckillVoucherServiceImpl seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ApiOrderService apiOrderService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;


    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private VoucherOrderService proxy;


    private String queueName = "stream.orders";


    /**
     * 这里我们使用lua脚本实现判断库存和用户是否下单，使用BlockQueue+开启独立线程的方法去创建订单
     */
    /*    @Override
     */

    /**
     * 这里我们使用lua脚本实现判断库存和用户是否下单，使用Stream消息队列实现阻塞队列
     * 发送订单消息到Java代码中，开启独立线程去不断地处理订单
     *//*
    public String seckillVoucher(Long voucherId) {
        //获取用户
        Long userId = UserHolder.getUser().getId();
        //2.1 订单id
        long orderId = redisIdWorker.nextId("order");
        //1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        proxy = (VoucherOrderService) AopContext.currentProxy();
        return Result.ok(orderId);
    }*/

    /*@Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //一人一单
        Long userId = voucherOrder.getId();
        //查询订单
        long count = this.query().eq("voucher_id", voucherOrder.getVoucherId())
                .eq("userId", userId).count();
        if (count > 0) {
            log.error("用户已经购买过了");
            return;
        }
        //扣减库存 乐观锁解决超卖问题
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0).update();
        //这种方式会导致许多线程都无法抢到优惠券
        //                .eq("stock",seckillVoucher.getStock()).update();
        if (!success) {
            log.error("库存不足");
        }
        this.save(voucherOrder);
    }*/
    @Override
    public BaseResponse seckillVoucher(Long voucherId, HttpServletRequest request) {
        //1.查询优惠券
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher == null) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "优惠券不存在");
        }
        LocalDateTime beginTime = seckillVoucher.getBeginTime();
        LocalDateTime endTime = seckillVoucher.getEndTime();
        //2. 秒杀是否开始或结束，如果尚未开始或已经结束则无法下单
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "活动已结束");
        }
        if (now.isBefore(beginTime)) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "活动尚未开始");
        }
        //3. 库存是否充足，不足则无法下单
        if (seckillVoucher.getStock() < 1) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "库存不足");
        }
        //4.库存充足，
        //5.根据优惠券id和用户id查询订单表，看用户是否已下单
        User user = apiOrderService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        //采用redis自实现分布式锁
//        ILock lock = new SimpleRedisLock("order:" + userId,stringRedisTemplate);
        //用redisson分布式锁
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //尝试获取锁
        boolean success = lock.tryLock();
        //获取锁失败，直接返回
        if (!success) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "不允许重复下单");
        }
        try {
            //获取当前对象的代理对象
            proxy = (VoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId, userId);
        } finally {
            //释放锁
            lock.unlock();
        }
        //这里直接锁方法的话，锁的粒度太大，我们只需要锁当前用户即可
        //但是toString()方法会创建新的字符串对象，所以使用inter()方法去常量池中找值相同的值
        //这里还需要，Spring管理事务是使用的AOP动态代理，
        // 所以我们应该使用动态代理对象来调用创建优惠券的方法
        /*synchronized (userId.toString().intern()) {
            //获取当前事务的代理对象
            proxy = (VoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }*/
    }

    /**
     * 创建优惠券
     *
     * @param voucherId
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public BaseResponse createVoucherOrder(Long voucherId, Long userId) {
        //一人一单
        //查询订单
        long count = this.query().eq("voucher_id", voucherId)
                .eq("user_id", userId).count();
        if (count > 0) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "用户已经购买过了");
        }
        //扣减库存 乐观锁解决超卖问题
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0).update();
        //这种方式会导致许多线程都无法抢到优惠券
        //                .eq("stock",seckillVoucher.getStock()).update();
        if (!success) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "库存不足");
        }
        //6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //6.1 订单id
        String orderId = redisIdWorker.nextOrderSn("order");
        voucherOrder.setId(orderId);
        //6.2用户id
        voucherOrder.setUserId(userId);
        //6.3 优惠券id
        voucherOrder.setVoucherId(voucherId);
        this.save(voucherOrder);
        //7.返回订单id
        return ResultUtils.success(orderId);
    }
}




