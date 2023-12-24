package com.zdzhai.apicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dongdong
 */
@Data
public class AliPayDtoMQ implements Serializable {

    /**
     * 交易状态
     */
    private String tradeStatus;

    /**
     * 订单id
     */
    private String orderSn;
}