package com.zdzhai.order.model.dto.voucherorder;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author dongdong
 * @TableName voucher_order
 */
@Data
public class BuyVoucherOrderDTO implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 下单的用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 0,普通券；1,秒杀券
     */
    private Integer type;

    private static final long serialVersionUID = 1L;


}