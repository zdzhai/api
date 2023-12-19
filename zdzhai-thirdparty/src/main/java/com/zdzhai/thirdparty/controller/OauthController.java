package com.zdzhai.thirdparty.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.CookieConstant;
import com.zdzhai.apicommon.model.entity.thirdparty.Oauth2LoginTo;
import com.zdzhai.apicommon.model.entity.thirdparty.vo.LoginUserVo;
import com.zdzhai.apicommon.service.IOauth2LoginServices;
import com.zdzhai.apicommon.utils.CookieUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 第三方登录
 */
@RequestMapping("/oauth")
@Controller
public class OauthController {

    @Value("${github.client_id}")
    private String github_client_id;

    @Value("${github.client_secret}")
    private String github_client_secret;

    @Value("${gitee.client_id}")
    private String gitee_client_id;

    @Value("${gitee.client_secret}")
    private String gitee_client_secret;

    @Value("${gitee.redirect_uri}")
    private String gitee_redirect_uri;

    @DubboReference
    private IOauth2LoginServices iOauth2LoginServices;


    @GetMapping("/github")
    public String github(@RequestParam("code") String code, HttpServletResponse res) throws IOException {
        String url = "https://github.com/login/oauth/access_token?client_id=" + github_client_id +
                "&client_secret=" + github_client_secret +
                "&code=" + code;
        HttpResponse response = null;
        try {
             response = HttpRequest.post(url)
                    .timeout(20000)//超时，毫秒
                    .execute();
        } catch (Exception e){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('请求超时，请重试')</script>");
            return  "redirect:http://localhost:8000/user/login";
        }
        if (response.getStatus() == 200){
            String s = response.body().toString();
            String[] split = s.split("&");
            String s1 = split[0];
            String[] split1 = s1.split("=");
            String token = split1[1];
            Oauth2LoginTo oauth2LoginTo = new Oauth2LoginTo();
            oauth2LoginTo.setAccess_token(token);
            oauth2LoginTo.setThird_party_name("github");
            //远程调用得到用户信息
            BaseResponse baseResponse = iOauth2LoginServices.oauth2Login(oauth2LoginTo);
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) {
                //添加cookie成功后,需要给前端响应用户信息。
                return "redirect:http://localhost:8000/user/login";
            }
        }
        return "redirect:http://localhost:8000/user/login";
    }

    @GetMapping("/gitee")
    public String gitee(@RequestParam("code") String code, HttpServletResponse res) throws IOException {
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code=" + code +
                "&client_id=" + gitee_client_id +
                "&client_secret=" + gitee_client_secret +
                "&redirect_uri=" + gitee_redirect_uri;
        HttpResponse response = null;
        try {
            response = HttpRequest.post(url)
                    .timeout(20000)//超时，毫秒
                    .execute();
        } catch (Exception e){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('请求超时，请重试')</script>");
            return  "redirect:http://localhost:8000/user/login";
        }
        if (response.getStatus() == 200){
            String s = response.body();
            Oauth2LoginTo oauth2LoginTo = JSONUtil.toBean(response.body(), Oauth2LoginTo.class);
            oauth2LoginTo.setThird_party_name("gitee");
            //远程调用得到用户信息
            BaseResponse baseResponse = iOauth2LoginServices.oauth2Login(oauth2LoginTo);
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) {
                //添加cookie成功后,需要给前端响应用户信息。
                return "redirect:http://localhost:8000/user/login";
            }
        }
        return "redirect:http://localhost:8000/user/login";
    }

    private boolean cookieResUtils(HttpServletResponse res, @NotNull BaseResponse baseResponse) throws IOException {
        if (baseResponse.getCode() != 0){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('登录失败')</script>");
            return false;
        }
        Object data = baseResponse.getData();
        LoginUserVo loginUserVo = JSONUtil.toBean(JSONUtil.parseObj(data), LoginUserVo.class);
        Cookie cookie = new Cookie(CookieConstant.headAuthorization,loginUserVo.getToken());
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        res.addCookie(cookie);
        res.setHeader("Access-Control-Allow-Credentials", "true");
        CookieUtils cookieUtils = new CookieUtils();
        String autoLoginContent = cookieUtils.generateAutoLoginContent(loginUserVo.getId().toString(), loginUserVo.getUserAccount());
        Cookie cookie1 = new Cookie(CookieConstant.autoLoginAuthCheck, autoLoginContent);
        cookie1.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        res.addCookie(cookie1);
        res.setHeader("Access-Control-Allow-Credentials", "true");
        return true;
    }
}
