package com.zdzhai.order.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.order.model.dto.order.ApiOrderAddRequest;
import com.zdzhai.order.model.dto.order.ApiOrderCancelRequest;
import com.zdzhai.order.model.dto.order.ApiOrderStatusInfoDto;
import com.zdzhai.order.model.vo.ApiOrderStatusVO;
import com.zdzhai.order.model.vo.OrderSnVO;
import com.zdzhai.order.service.ApiOrderService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

/**
 * @author dongdong
 */
@RestController
@Api("订单接口")
@RequestMapping("/order")
public class OrderController {

    @Resource
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
        apiOrderService.generateToken(request, response);
        return ResultUtils.success("ok");
    }


    /**
     * 创建订单
     * @param apiOrderAddRequest
     * @return
     */
    @PostMapping("/generateOrderSn")
    public BaseResponse<OrderSnVO> generateOrderSn(ApiOrderAddRequest apiOrderAddRequest,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) throws ExecutionException, InterruptedException {
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
    public BaseResponse<String> cancelOrderSn(ApiOrderCancelRequest apiOrderCancelRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        String ans = apiOrderService.cancelOrderSn(apiOrderCancelRequest,request,response);
        return ResultUtils.success("取消订单成功");
    }

    /**
     * 获取当前登录用户的status订单信息
     * @param statusInfoDto
     * @param request
     * @return
     */
    @PostMapping("/getCurrentOrderInfo")
    public BaseResponse<Page<ApiOrderStatusVO>> getCurrentOrderInfo(ApiOrderStatusInfoDto statusInfoDto, HttpServletRequest request){
        Page<ApiOrderStatusVO> apiOrderStatusVOPage = apiOrderService.getCurrentOrderInfo(statusInfoDto,request);
        return ResultUtils.success(apiOrderStatusVOPage);
    }

}
