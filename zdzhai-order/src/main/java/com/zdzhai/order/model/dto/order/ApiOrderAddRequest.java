package com.zdzhai.order.model.dto.order;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建订单dto
 * @author dongdong
 */
@Data
public class ApiOrderAddRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 购买数量
     */
    private Long orderNum;

    /**
     * 单价
     */
    private Integer charging;

    /**
     * 交易金额
     */
    private Long totalAmount;
}
