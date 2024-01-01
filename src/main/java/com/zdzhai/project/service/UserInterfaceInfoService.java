package com.zdzhai.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.model.dto.UserInterfaceInfoUpdateRequest;
import com.zdzhai.apicommon.model.entity.UserInterfaceInfo;
import com.zdzhai.project.model.vo.UserInterfaceLeftNumVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     * 更新用户接口关系数据
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @param response
     * @return
     */
    boolean updateUserInterfaceInfo(UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response);

    /**
     * 调用接口次数统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 创建用户接口信息
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean createUserInterfaceInfo(long interfaceInfoId, long userId);

    /**
     * 获取当前登录用户的接口剩余调用次数
     * @param loginUserId
     * @return
     */
    List<UserInterfaceLeftNumVO> getUserInterfaceLeftNum(Long loginUserId);

    /**
     * 远程更新用户接口信息
     * @param userInterfaceInfoUpdateRequest
     * @return
     */
    boolean updateUserInterfaceInfoForRemote(UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest);
}
