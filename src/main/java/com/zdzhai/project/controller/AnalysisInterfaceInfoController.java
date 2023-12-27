package com.zdzhai.project.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.project.annotation.AuthCheck;
import com.zdzhai.project.mapper.UserInterfaceInfoMapper;
import com.zdzhai.project.model.vo.InterfaceInfoVO;
import com.zdzhai.project.model.vo.UserInterfaceInfoVO;
import com.zdzhai.project.service.InterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
