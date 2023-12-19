package com.zdzhai.project.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * echarts需要返回的数据
 * @author dongdong
 */
@Data
public class EchartsVO implements Serializable {
    private Long count;

    private String date;
}
