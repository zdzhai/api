package com.zdzhai.apicommon.service;


import com.zdzhai.apicommon.model.entity.InterfaceInfo;


/**
* @author dongdong
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-03-17 21:30:28
*/
public interface InnerInterfaceInfoService {

    /**
     *   从数据库中查询模拟接口是否存在，以及请求方法时候匹配
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String path, String method);

    /**
     *   从数据库中查询模拟接口是否存在，以及请求方法时候匹配
     * @param interfaceInfoId
     * @return
     */
    InterfaceInfo getById(Long interfaceInfoId);
}
