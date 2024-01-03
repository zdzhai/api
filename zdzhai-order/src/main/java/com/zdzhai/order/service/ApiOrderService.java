package com.zdzhai.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.dto.ApiOrderTokenRequest;
import com.zdzhai.apicommon.model.entity.ApiOrder;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.order.model.dto.order.ApiOrderAddRequest;
import com.zdzhai.order.model.dto.order.ApiOrderCancelRequest;
import com.zdzhai.order.model.dto.order.ApiOrderStatusInfoDto;
import com.zdzhai.order.model.vo.ApiOrderStatusVO;
import com.zdzhai.order.model.vo.OrderSnVO;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

/**
* @author 62618
* @description 针对表【api_order】的数据库操作Service
* @createDate 2023-12-17 16:48:17
*/
public interface ApiOrderService extends IService<ApiOrder> {

    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     * @param request
     * @param response
     * @return
     */
    void generateToken(HttpServletRequest request,
                       HttpServletResponse response);

    /**
     * 生成订单接口
     * @param apiOrderAddRequest
     * @param request
     * @param response
     * @return
     */
    OrderSnVO generateOrderSn(ApiOrderAddRequest apiOrderAddRequest,
                              HttpServletRequest request,
                              HttpServletResponse response) throws ExecutionException, InterruptedException;

    /**
     * 取消订单
     * @param apiOrderCancelRequest
     * @param request
     * @param response
     * @return
     */
    String cancelOrderSn(ApiOrderCancelRequest apiOrderCancelRequest, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据订单号修改订单状态
     * @param orderSn
     * @param status
     * @return
     */
    int updateApiOrderStatusByOrderSn(String orderSn, int status);

    /**
     * 根据订单号查找订单信息
     * @param orderSn
     * @return
     */
    ApiOrder getApiOrderByOrderSn(String orderSn);

    /**
     * 获取当前登录用户的status订单信息
     * @param statusInfoDto
     * @param request
     * @return
     */
    Page<ApiOrderStatusVO> getCurrentOrderInfo(ApiOrderStatusInfoDto statusInfoDto, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
}
