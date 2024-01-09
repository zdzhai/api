package com.zdzhai.order.controller;


import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.order.model.dto.voucherorder.BuyVoucherOrderDTO;
import com.zdzhai.order.service.VoucherOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dongdong
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    @PostMapping("seckill")
    public BaseResponse<String> seckillVoucher(@RequestBody BuyVoucherOrderDTO buyVoucherOrderDTO, HttpServletRequest request) throws InterruptedException {
        return voucherOrderService.seckillVoucher(buyVoucherOrderDTO, request);
    }
}
