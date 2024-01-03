package com.zdzhai.order.service;

import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.order.model.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 62618
* @description 针对表【voucher_order】的数据库操作Service
* @createDate 2023-12-26 19:37:17
*/
public interface VoucherOrderService extends IService<VoucherOrder> {
    /**
     * 秒杀优惠券
     * @param voucherId
     * @return
     */
    BaseResponse<String> seckillVoucher(Long voucherId,
                                        HttpServletRequest request) throws InterruptedException;

    /**
     * 创建优惠券
     * @param voucherId
     * @return
     */
    BaseResponse<String> createVoucherOrder(Long voucherId, Long userId);
}
