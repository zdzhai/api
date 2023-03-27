package com.zdzhai.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.model.entity.UserInterfaceInfo;

/**
* @author 62618
* @description 针对表【user_interface_info(用户调用接口信息表)】的数据库操作Service
* @createDate 2023-03-24 19:57:31
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 校验
     *
     * @param userInterfaceInfo
     * @param add 是否为创建校验
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 条用接口次数统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
