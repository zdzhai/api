package com.zdzhai.apicommon.service;


/**
* @author 62618
* @description 针对表【user_interface_info(用户调用接口信息表)】的数据库操作Service
* @createDate 2023-03-24 19:57:31
*/
public interface InnerUserInterfaceInfoService {

    /** 
     * 条用接口次数统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
