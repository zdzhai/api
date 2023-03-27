package com.zdzhai.apicommon.service;


import com.zdzhai.apicommon.model.entity.User;

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
}
