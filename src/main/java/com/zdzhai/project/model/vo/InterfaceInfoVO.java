package com.zdzhai.project.model.vo;

import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.project.model.entity.Post;
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
public class InterfaceInfoVO extends InterfaceInfo {

    /**
     * 总调用次数
     */
    private Long totalNum;

    private static final long serialVersionUID = 1L;
}