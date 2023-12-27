package com.zdzhai.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.project.mapper.InterfaceChargingMapper;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingAddRequest;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingQueryRequest;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingUpdateRequest;
import com.zdzhai.project.model.entity.InterfaceCharging;
import com.zdzhai.project.model.vo.InterfaceChargingVO;
import com.zdzhai.project.service.InterfaceChargingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 62618
* @description 针对表【interface_charging】的数据库操作Service实现
* @createDate 2023-12-11 12:09:00
*/
@Service
public class InterfaceChargingServiceImpl extends ServiceImpl<InterfaceChargingMapper, InterfaceCharging>
    implements InterfaceChargingService{

    @Resource
    private InterfaceChargingMapper interfaceChargingMapper;

    /**
     * 更新接口费用信息
     * @param chargingUpdateRequest
     * @return
     */
    @Override
    public void updateChargingInfo(InterfaceChargingUpdateRequest chargingUpdateRequest) {
        Long interfaceId = chargingUpdateRequest.getInterfaceId();
        Integer availableCounts = chargingUpdateRequest.getAvailableCounts();
        try {
            this.update(new UpdateWrapper<InterfaceCharging>().eq("interfaceId",interfaceId)
                    .set("availableCounts", availableCounts));
        }catch (Exception e){
            throw new BusinessException( ErrorCode.PARAMS_ERROR,"更新失败");
        }
    }

    /**
     * 新增接口费用信息
     * @param chargingAddRequest
     * @return
     */
    @Override
    public int addChargingInfo(InterfaceChargingAddRequest chargingAddRequest) {
        Long interfaceId = chargingAddRequest.getInterfaceId();
        Integer charging = chargingAddRequest.getCharging();
        Integer availableCounts = chargingAddRequest.getAvailableCounts();
        InterfaceCharging interfaceCharging = new InterfaceCharging();
        BeanUtils.copyProperties(chargingAddRequest, interfaceCharging);
        int newId;
        try {
            newId = interfaceChargingMapper.insert(interfaceCharging);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入失败");
        }
        return newId;
    }

    /**
     * 获取接口费用信息
     * @return
     */
    @Override
    public Page<InterfaceChargingVO> getChargingInfos(InterfaceChargingQueryRequest chargingQueryRequest) {
        InterfaceCharging interfaceChargingQuery = new InterfaceCharging();
        BeanUtils.copyProperties(chargingQueryRequest, interfaceChargingQuery);
        long current = chargingQueryRequest.getCurrent();
        long size = chargingQueryRequest.getPageSize();
        Page<InterfaceChargingVO> chargingVOList = interfaceChargingMapper.selectOnlinePage(new Page<>(current, size));
        return chargingVOList;
    }
}




