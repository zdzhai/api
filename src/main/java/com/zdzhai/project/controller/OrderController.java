package com.zdzhai.project.controller;


import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ResultUtils;
import com.zdzhai.project.model.dto.order.ApiOrderAddRequest;
import com.zdzhai.project.model.dto.order.ApiOrderCancelRequest;
import com.zdzhai.project.model.vo.OrderSnVO;
import com.zdzhai.project.service.ApiOrderService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

/**
 * @author YukeSeko
 */
@RestController
@Api("订单接口")
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private ApiOrderService apiOrderService;


    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     * @param
     * @param response
     * @return
     */
    @GetMapping("/generateToken")
    public BaseResponse<String> generateToken(HttpServletRequest request,
                                      HttpServletResponse response){
        apiOrderService.generateToken(request,response);
        return ResultUtils.success("ok");
    }


    /**
     * 创建订单
     * @param apiOrderAddRequest
     * @return
     */
    @PostMapping("/generateOrderSn")
    public BaseResponse<OrderSnVO> generateOrderSn(ApiOrderAddRequest apiOrderAddRequest, HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException {
        OrderSnVO orderSnVO = apiOrderService.generateOrderSn(apiOrderAddRequest, request, response);
        return ResultUtils.success(orderSnVO);
    }

    /**
     * 取消订单
     * @param apiOrderCancelRequest
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/cancelOrderSn")
    public BaseResponse<String> cancelOrderSn(ApiOrderCancelRequest apiOrderCancelRequest, HttpServletRequest request, HttpServletResponse response) {
        apiOrderService.cancelOrderSn(apiOrderCancelRequest,request,response);
        return ResultUtils.success("取消订单成功");
    }

}
