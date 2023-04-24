package com.zdzhai.project.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.common.ResultUtils;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.model.entity.UserInterfaceInfo;
import com.zdzhai.project.annotation.AuthCheck;
import com.zdzhai.project.common.*;
import com.zdzhai.project.constant.CommonConstant;

import com.zdzhai.project.mapper.UserInterfaceInfoMapper;
import com.zdzhai.project.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.zdzhai.project.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.zdzhai.project.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.zdzhai.project.model.dto.interfaceInfo.InterfaceInfoUpdateRequest;
import com.zdzhai.project.model.enums.InterfaceInfoStatusEnum;
import com.zdzhai.project.model.vo.InterfaceInfoVO;
import com.zdzhai.project.model.vo.UserInterfaceInfoVO;
import com.zdzhai.project.service.InterfaceInfoService;
import com.zdzhai.project.service.UserInterfaceInfoService;
import com.zdzhai.project.service.UserService;
import com.zdzhai.zdzhaiclientsdk.client.ZdzhaiApiClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分析接口信息接口
 *
 * @author dongdong
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisInterfaceInfoController {


    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @GetMapping
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVO>> listTOPInvokeInterfaceInfo(){
        List<UserInterfaceInfoVO> userInterfaceInfoVOList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        Map<Long, List<UserInterfaceInfoVO>> interfaceInfoIdObjMap = userInterfaceInfoVOList
                .stream()
                .collect(Collectors.groupingBy(UserInterfaceInfoVO::getInterfaceInfoId));

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        Set<Long> set = interfaceInfoIdObjMap.keySet();
        queryWrapper.in("id", set);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        if (CollectionUtil.isEmpty(interfaceInfoList)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            long totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(interfaceInfoVOList);
    }

}
