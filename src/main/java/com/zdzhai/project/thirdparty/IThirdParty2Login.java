package com.zdzhai.project.thirdparty;

import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.entity.thirdparty.Oauth2LoginTo;

/**
 * @author dongdong
 * @Date 2023/12/4 19:44
 */
public interface IThirdParty2Login {

    BaseResponse oauth2Login(Oauth2LoginTo oauth2LoginTo);
}
