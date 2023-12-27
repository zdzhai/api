package com.zdzhai.apicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 调用用户Id
     */
    private Long userId;

    /**
     * 接口Id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Long totalNum;

    /**
     * 剩余调用次数
     */
    private Long leftNum;

    /**
     * 购买数量
     */
    private Long orderNum;

    /**
     * （0-正常，1-禁用）
     */
    private Integer status;
}