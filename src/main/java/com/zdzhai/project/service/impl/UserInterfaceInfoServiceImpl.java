package com.zdzhai.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.model.entity.UserInterfaceInfo;
import com.zdzhai.project.mapper.UserInterfaceInfoMapper;
import com.zdzhai.project.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import com.zdzhai.project.model.vo.UserInterfaceLeftNumVO;
import com.zdzhai.project.service.UserInterfaceInfoService;
import com.zdzhai.project.service.UserService;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author 62618
 * @description 针对表【user_interface_info(用户调用接口信息表)】的数据库操作Service实现
 * @createDate 2023-03-24 19:57:31
 */
@Service
@EnableAspectJAutoProxy(exposeProxy = true,proxyTargetClass = true)
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private UserService userService;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = userInterfaceInfo.getId();
        Long userId = userInterfaceInfo.getUserId();
        Long leftNum = userInterfaceInfo.getLeftNum();

        // 创建时，所有参数必须非空
        if (add) {
            if (userId == null || userId <= 0 || id == null || id <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口或用户不存在");
            }
        }
        if (leftNum < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"剩余次数不能小于0");
        }
    }

    @Override
    public boolean updateUserInterfaceInfo(UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        User user = userService.getLoginUser(request,response);
        long interfaceInfoId = userInterfaceInfoUpdateRequest.getInterfaceInfoId();
        // 判断接口是否存在
        UserInterfaceInfo dbUserInterfaceInfo = this.getById(interfaceInfoId);
        if (dbUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!dbUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request,response)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int updateRow = userInterfaceInfoMapper.updateUserInterfaceInfoLeftNum(userInterfaceInfoUpdateRequest);
        return updateRow != 0;
    }

    @Override

    public boolean invokeCount(long interfaceInfoId, long userId) {
        //1.校验参数
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        synchronized (Long.toString(userId).intern()) {
            UserInterfaceInfoService proxy = (UserInterfaceInfoService) AopContext.currentProxy();
            return proxy.createUserInterfaceInfo(interfaceInfoId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createUserInterfaceInfo(long interfaceInfoId, long userId) {
        //2.查数据库
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo userInterfaceInfo;
        userInterfaceInfo = this.getOne(queryWrapper);
        if (userInterfaceInfo == null) {
            //创建数据
           userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
            userInterfaceInfo.setTotalNum(0L);
            userInterfaceInfo.setLeftNum(20L);
            userInterfaceInfo.setStatus(0);
            userInterfaceInfo.setIsDelete(0);
            boolean save = this.save(userInterfaceInfo);
            if (!save) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        Long leftNum = userInterfaceInfo.getLeftNum();
        if (leftNum <= 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无调用次数！");
        }
        //3. 改数据库
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.setSql("leftNum = leftNum - 1,totalNum = totalNum + 1");
        return this.update(updateWrapper);
    }

    /**
     * 获取当前登录用户的接口剩余调用次数
     * @param loginUserId
     * @return
     */
    @Override
    public List<UserInterfaceLeftNumVO> getUserInterfaceLeftNum(Long loginUserId) {
        List<UserInterfaceLeftNumVO>  userInterfaceLeftNumVo =  userInterfaceInfoMapper.getUserInterfaceLeftNum(loginUserId);
        return userInterfaceLeftNumVo;
    }
}




