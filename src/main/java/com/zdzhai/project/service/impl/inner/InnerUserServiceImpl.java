package com.zdzhai.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.service.InnerUserService;
import com.zdzhai.project.mapper.UserMapper;
import com.zdzhai.project.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author dongdong
 * @Date 2023/3/26 21:09
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    /**
     * 根据accessKey获取用户的信息（secretKey）
     * @param accessKey
     * @return
     */
    @Override
    public User getInvokeUser(String accessKey) {
        if (StringUtils.isBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey",accessKey);

        return userMapper.selectOne(queryWrapper);
    }

}
