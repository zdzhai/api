package com.zdzhai.project.service.impl.inner;

import com.zdzhai.apicommon.model.dto.UserInterfaceInfoUpdateRequest;
import com.zdzhai.apicommon.service.InnerUserInterfaceInfoService;
import com.zdzhai.project.service.UserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dongdong
 * @Date 2023/3/26 21:09
 */
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public boolean updateUserInterfaceInfo(UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest, HttpServletRequest request, HttpServletResponse response) {
        return userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoUpdateRequest, request, response);
    }
}
