package com.zdzhai.project.model.dto.tuwei;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author dongdong
 */
@Data
public class TuweiUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 鸡汤内容
     */
    private String sentence;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}