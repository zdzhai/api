package com.zdzhai.project.model.vo;

import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子视图
 *
 * @author dongdong
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoLeftNumVO extends InterfaceInfo {

    /**
     * 总调用次数
     */
    private Long leftNum;

    private static final long serialVersionUID = 1L;
}