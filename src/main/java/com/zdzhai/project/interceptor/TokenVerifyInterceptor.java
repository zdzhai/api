package com.zdzhai.project.interceptor;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.common.CookieConstant;
import com.zdzhai.project.common.TokenUtils;
import com.zdzhai.project.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dongdong
 * @Date 2023/4/15 10:16
 */
@Component
public class TokenVerifyInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.根据request获取cookie，然后根据authorization获得token
        //验证token是否有效，是否过期
        Cookie[] cookies = request.getCookies();
        String authorization =null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (CookieConstant.headAuthorization.equals(name)){
                authorization = cookie.getValue();
            }
        }
        // 1、判断是否存在
        if (null == authorization){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2、验证token是否合法，判断当前登录用户和token中的用户是否相同
        TokenUtils tokenUtils = new TokenUtils();
        boolean verifyToken = tokenUtils.verifyToken(authorization);
        if (!verifyToken){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        // 3、验证token是否过期
        boolean verifyTime = tokenUtils.verifyTokenTime(authorization);
        if (!verifyTime){
            //过期了需要重新登录
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"登录状态过期，请重新登录");
       }
        User loginUser = userService.getLoginUser(request, response);
        JWT jwt = JWTUtil.parseToken(authorization);
        String userAccount = (String) jwt.getPayload("userAccount");
        String id = (String) jwt.getPayload("id");
        if (userAccount == null || id == null){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        if (!loginUser.getId().toString().equals(id) || !loginUser.getUserAccount().equals(userAccount)){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        return true;
    }
}
