package com.zdzhai.project.model.dto.tuwei;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class TuweiAddRequest implements Serializable {

    /**
     * 鸡汤内容
     */
    private String sentence;
    private static final long serialVersionUID = 1L;
}