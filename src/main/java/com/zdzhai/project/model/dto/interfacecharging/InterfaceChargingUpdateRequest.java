package com.zdzhai.project.model.dto.interfacecharging;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author dongdong
 */
@Data
public class InterfaceChargingUpdateRequest implements Serializable {

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