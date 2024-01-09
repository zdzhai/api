package com.zdzhai.order.service;

import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.order.model.dto.voucherorder.BuyVoucherOrderDTO;
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
     * @param buyVoucherOrderDTO
     * @return
     */
    BaseResponse<String> seckillVoucher(BuyVoucherOrderDTO buyVoucherOrderDTO,
                                        HttpServletRequest request) throws InterruptedException;

    /**
     * 创建秒杀优惠券订单
     * @param voucherId
     * @return
     */
    BaseResponse<String> createSeckillVoucherOrder(Long voucherId, Long userId);
}
