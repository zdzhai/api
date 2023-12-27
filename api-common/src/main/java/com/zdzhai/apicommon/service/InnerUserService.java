package com.zdzhai.apicommon.service;


import com.zdzhai.apicommon.model.entity.User;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户服务
 *
 * @author dongdong
 */
public interface InnerUserService {

    /**
     * 要去数据库查用户的信息 然后拿到accessKey
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

    /**
     * 获取当前登录用户
     * @param request
     * @param response
     * @return
     */
    User getLoginUser(HttpServletRequest request,
                      HttpServletResponse response);
}
