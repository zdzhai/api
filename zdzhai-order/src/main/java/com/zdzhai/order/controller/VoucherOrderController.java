package com.zdzhai.order.controller;


import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.order.service.VoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("seckill/{id}")
    public BaseResponse<String> seckillVoucher(@PathVariable("id") Long voucherId, HttpServletRequest request) throws InterruptedException {
        return voucherOrderService.seckillVoucher(voucherId, request);
    }
}
