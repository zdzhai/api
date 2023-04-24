package com.zdzhai.project.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AkVO implements Serializable {

    /**
     *  accessKey
     */
    private String accesskey;

    /**
     * 接口状态(0-启用, 1-未启用)
     */
    private Integer  status;;

}