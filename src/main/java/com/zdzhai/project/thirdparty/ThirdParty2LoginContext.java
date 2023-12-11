package com.zdzhai.project.thirdparty;

import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.entity.thirdparty.Oauth2LoginTo;

/**
 * @author dongdong
 * @Date 2023/12/4 19:48
 */
public class ThirdParty2LoginContext {
    private IThirdParty2Login thirdParty2Login;

    public ThirdParty2LoginContext(String thirdPartyName) {
        //todo 改为使用反射动态生成
        switch (thirdPartyName) {
            case "github":
                this.thirdParty2Login = new GithubLogin();
                break;
            case "gitee":
                this.thirdParty2Login = new GiteeLogin();
                break;
        }
    }

    public BaseResponse getResponse(Oauth2LoginTo oauth2LoginTo) {
        return this.thirdParty2Login.oauth2Login(oauth2LoginTo);
    }
}
