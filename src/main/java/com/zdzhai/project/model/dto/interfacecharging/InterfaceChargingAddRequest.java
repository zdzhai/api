package com.zdzhai.project.model.dto.interfacecharging;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.zdzhai.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author dongdong
 */
@Data
public class InterfaceChargingAddRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 接口费用
     */
    private Integer charging;

    /**
     * 接口最大可购买次数
     */
    private Integer availableCounts;
}