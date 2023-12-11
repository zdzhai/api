package com.zdzhai.apicommon.service;


import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.entity.thirdparty.Oauth2LoginTo;

/**
 * @author dongdong
 */
public interface IOauth2LoginServices {

    /**
     * 第三方登录
     * @param oauth2LoginTo
     * @return
     */
    BaseResponse oauth2Login(Oauth2LoginTo oauth2LoginTo);
}
