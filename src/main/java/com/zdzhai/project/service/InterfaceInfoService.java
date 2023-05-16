package com.zdzhai.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.project.model.vo.InterfaceInfoLeftNumVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* @author 62618
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-03-17 21:30:28
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 根据 id 获取接口详细信息和用户调用次数
     * @param id
     * @param request
     * @param  response
     * @return
     */
    InterfaceInfoLeftNumVO getInterfaceInfoById(long id, HttpServletRequest request, HttpServletResponse response);
}
