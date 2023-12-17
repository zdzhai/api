package com.zdzhai.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingAddRequest;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingQueryRequest;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingUpdateRequest;
import com.zdzhai.project.model.entity.InterfaceCharging;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.project.model.vo.InterfaceChargingVO;

import java.util.List;

/**
* @author 62618
* @description 针对表【interface_charging】的数据库操作Service
* @createDate 2023-12-11 12:09:00
*/
public interface InterfaceChargingService extends IService<InterfaceCharging> {

    /**
     * 更新接口费用信息
     * @param chargingUpdateRequest
     * @return
     */
    void updateChargingInfo(InterfaceChargingUpdateRequest chargingUpdateRequest);

    /**
     * 新增接口费用信息
     * @param chargingAddRequest
     * @return
     */
    int addChargingInfo(InterfaceChargingAddRequest chargingAddRequest);

    /**
     * 获取接口费用信息
     * @return
     */
    Page<InterfaceChargingVO> getChargingInfos(InterfaceChargingQueryRequest chargingQueryRequest);
}
