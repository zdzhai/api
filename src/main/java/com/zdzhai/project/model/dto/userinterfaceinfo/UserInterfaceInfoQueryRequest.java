package com.zdzhai.project.model.dto.userinterfaceinfo;

import com.zdzhai.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author dongdong
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInterfaceInfoQueryRequest extends PageRequest implements Serializable {

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
     * （0-正常，1-禁用）
     */
    private Integer status;
}