package com.zdzhai.thirdparty.controller;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.thirdparty.config.AliPayClientConfig;
import com.zdzhai.thirdparty.constant.RedisConstant;
import com.zdzhai.thirdparty.model.dto.AliPayDto;
import com.zdzhai.thirdparty.model.entity.AlipayInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author dongdong
 */
@Controller
@RequestMapping("/alipay")
public class AliPayController {

    @Resource
    private AliPayClientConfig aliPayClientConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 调用支付请求
     * @param aliPay
     * @param response
     * @throws Exception
     */
    @PostMapping("/pay")
    @ResponseBody
    public void payController(@RequestBody AliPayDto aliPay, HttpServletResponse response) throws IOException {
        AlipayClient alipayClient = aliPayClientConfig.alipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(aliPayClientConfig.NotifyUrl);//设置支付成功的异步回调页面
        request.setReturnUrl(aliPayClientConfig.tradeSuccessUrl);//设置回调友好页面
        String subject = aliPay.getSubject() + "-" + aliPay.getTraceNo();
        request.setBizContent("{\"out_trade_no\":\"" + aliPay.getTraceNo() + "\","
                + "\"total_amount\":\"" + aliPay.getTotalAmount() + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String form = "";
        try {
            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
        } catch (AlipayApiException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求支付失败请重试");
        }
        response.setContentType("text/html;charset=" + aliPayClientConfig.CHARSET);
        response.getWriter().write(form);// 直接将完整的表单html输出到页面
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * 支付成功回调
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public synchronized void payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            /**
             * 交易名称: 获取用户姓名接口
             * 交易状态: TRADE_SUCCESS
             * 支付宝交易凭证号: 2023032122001438770502011170
             * 商户订单号: 202303211644560221638099358014627841
             * 交易金额: 10.00
             * 买家在支付宝唯一id: 2088622958038771
             * 买家付款时间: 2023-03-21 16:45:30
             * 买家付款金额: 10.00
             */
            // 支付宝验签
            if (AlipaySignature.rsaCheckV1(params, aliPayClientConfig.ALIPAY_PUBLIC_KEY, aliPayClientConfig.CHARSET, aliPayClientConfig.SIGN_TYPE)) {
                // 幂等性保证：判断该订单号是否被处理过
                Object outTradeNo = stringRedisTemplate.opsForValue().get(RedisConstant.ALIPAY_TRADE_INFO + params.get("out_trade_no"));
                if (null == outTradeNo) {
                    // 验签通过，将订单信息存入数据库
                    AlipayInfo alipayInfo = new AlipayInfo();
                    alipayInfo.setSubject(params.get("subject"));
                    alipayInfo.setTradeStatus(params.get("trade_status"));
                    alipayInfo.setTradeNo(params.get("trade_no"));
                    alipayInfo.setOrderSn(params.get("out_trade_no"));
                    alipayInfo.setTotalAmount(Long.valueOf(params.get("total_amount")));
                    alipayInfo.setBuyerId(params.get("buyer_id"));
                    alipayInfo.setGmtPayment(DateUtil.parse(params.get("gmt_payment")));
                    alipayInfo.setBuyerPayAmount(Long.valueOf(params.get("buyer_pay_amount")));
                    //alipayInfoService.save(alipayInfo);
                    Gson gson = new Gson();
                    String alipayInfoToJson = gson.toJson(alipayInfo);
                    //同时将交易结果存入redis中去，保证支付请求幂等性
                    stringRedisTemplate.opsForValue().set(RedisConstant.ALIPAY_TRADE_INFO + alipayInfo.getOrderSn(), alipayInfoToJson, 30, TimeUnit.MINUTES);
                    //修改数据库，完成整个订单功能
                    //rabbitOrderPaySuccessUtils.sendOrderPaySuccess(params.get("out_trade_no"));
                    System.out.println("支付宝验签成功");
                }
            }
        }
    }
    /**
     * 查询订单的支付状态
     * @param orderSn
     * @return
     */
    @GetMapping("/queryTradeStatus")
    @ResponseBody
    public BaseResponse<AlipayInfo> queryTradeStatus(@RequestParam String orderSn){
        if (null == orderSn){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String alipayInfoJson = stringRedisTemplate.opsForValue().get(RedisConstant.ALIPAY_TRADE_INFO + orderSn);
        Gson gson = new Gson();
        AlipayInfo alipayInfo = gson.fromJson(alipayInfoJson, AlipayInfo.class);
        if (null == alipayInfo){
            return ResultUtils.success(null);
        }
        String tradeStatus = alipayInfo.getTradeStatus();
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            return ResultUtils.success(alipayInfo);
        }
        return ResultUtils.success(null);
    }
}