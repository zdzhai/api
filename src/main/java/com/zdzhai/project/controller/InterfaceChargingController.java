package com.zdzhai.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingAddRequest;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingQueryRequest;
import com.zdzhai.project.model.dto.interfacecharging.InterfaceChargingUpdateRequest;
import com.zdzhai.project.model.vo.InterfaceChargingVO;
import com.zdzhai.project.service.InterfaceChargingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dongdong
 */
@RestController
@RequestMapping("/charging")
public class InterfaceChargingController {

    @Autowired
    private InterfaceChargingService interfaceChargingService;

    /**
     * 新增接口费用信息
     * @param chargingAddRequest
     * @return
     */
    @PostMapping("/addChargingInfo")
    public BaseResponse<Integer> addChargingInfo(@RequestBody InterfaceChargingAddRequest chargingAddRequest){
        if (chargingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int id = interfaceChargingService.addChargingInfo(chargingAddRequest);
        return ResultUtils.success(id);
    }


    /**
     * 更新接口费用信息
     * @param chargingUpdateRequest
     * @return
     */
    @PostMapping("/updateChargingInfo")
    public BaseResponse updateChargingInfo(@RequestBody InterfaceChargingUpdateRequest chargingUpdateRequest){
        interfaceChargingService.updateChargingInfo(chargingUpdateRequest);
        return ResultUtils.success("修改成功");
    }

    /**
     * 获取接口费用信息
     * @param chargingQueryRequest
     * @return
     */
    @PostMapping("/getChargingInfos")
    public BaseResponse<Page<InterfaceChargingVO>> listChargingInfos(@RequestBody InterfaceChargingQueryRequest chargingQueryRequest){
        if (chargingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<InterfaceChargingVO> chargingVOList = interfaceChargingService.getChargingInfos(chargingQueryRequest);
        return ResultUtils.success(chargingVOList);
    }
}
