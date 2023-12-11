package com.zdzhai.project.service.impl;

import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.entity.thirdparty.Oauth2LoginTo;
import com.zdzhai.apicommon.service.IOauth2LoginServices;
import com.zdzhai.project.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author dongdong
 * @Date 2023/12/7 21:07
 * 通过第三方调用
 */
@DubboService
public class Oauth2LoginServiceImpl implements IOauth2LoginServices {

    @Resource
    private UserService userService;
    @Override
    public BaseResponse oauth2Login(Oauth2LoginTo oauth2LoginTo) {

        return userService.oauth2Login(oauth2LoginTo);
    }
}
