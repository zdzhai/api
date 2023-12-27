package com.zdzhai.project.thirdparty;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.entity.thirdparty.Oauth2LoginTo;
import com.zdzhai.apicommon.model.entity.thirdparty.vo.LoginUserVo;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.project.common.Oauth2LoginUtils;

/**
 * @author dongdong
 * @Date 2023/12/4 19:47
 */
public class GithubLogin implements IThirdParty2Login{

    private Oauth2LoginUtils oauth2LoginUtils = new Oauth2LoginUtils();


    @Override
    public BaseResponse oauth2Login(Oauth2LoginTo oauth2LoginTo) {
        String accessToken = oauth2LoginTo.getAccess_token();
        HttpResponse response = HttpRequest.get("https://api.github.com/user")
                .header("Authorization","Bearer "+accessToken)
                .timeout(20000)
                //超时，毫秒
                .execute();
        JSONObject obj = JSONUtil.parseObj(response.body());
        String userAccount = String.valueOf(obj.get("login"));
        String name = String.valueOf(obj.get("name"));
        String userAvatar = String.valueOf(obj.get("avatar_url"));
        //查数据库校验
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setUserName(name);
        loginUserVo.setUserAvatar(userAvatar);
        loginUserVo.setUserAccount(userAccount);
        loginUserVo = oauth2LoginUtils.checkOauth2Login(loginUserVo);
        return ResultUtils.success(loginUserVo);
    }
}
