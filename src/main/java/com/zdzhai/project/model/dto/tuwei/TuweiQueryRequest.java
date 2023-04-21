package com.zdzhai.project.model.dto.tuwei;

import com.zdzhai.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 *
 * @author dongdong
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TuweiQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 鸡汤
     */
    private String sentence;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}