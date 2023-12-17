package com.zdzhai.project.model.dto.interfacecharging;

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
public class InterfaceChargingQueryRequest extends PageRequest implements Serializable {


}