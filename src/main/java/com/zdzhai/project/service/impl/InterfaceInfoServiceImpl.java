package com.zdzhai.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.model.entity.UserInterfaceInfo;
import com.zdzhai.project.mapper.InterfaceInfoMapper;
import com.zdzhai.project.model.vo.InterfaceInfoLeftNumVO;
import com.zdzhai.project.service.InterfaceInfoService;
import com.zdzhai.project.service.UserInterfaceInfoService;
import com.zdzhai.project.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author 62618
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2023-03-17 21:30:28
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        Long id = interfaceInfo.getId();
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestParams = interfaceInfo.getRequestParams();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        Integer status = interfaceInfo.getStatus();
        String method = interfaceInfo.getMethod();
        Long userid = interfaceInfo.getUserId();
        Date createTime = interfaceInfo.getCreateTime();
        Date updateTime = interfaceInfo.getUpdateTime();
        Integer isDelete = interfaceInfo.getIsDelete();

        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }

    @Override
    public InterfaceInfoLeftNumVO getInterfaceInfoById(long id,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request, response);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        InterfaceInfo interfaceInfo = this.getById(id);
        InterfaceInfoLeftNumVO interfaceInfoLeftNumVO = new InterfaceInfoLeftNumVO();
        BeanUtils.copyProperties(interfaceInfo,interfaceInfoLeftNumVO);
        UserInterfaceInfo one = userInterfaceInfoService
                .getOne(new QueryWrapper<UserInterfaceInfo>()
                        .eq("userId", userId).eq("interfaceInfoId", id));
        if (null == one){
            //请求数据不存在时，创建一个新的，并将调用次数设置为0
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(id);
            userInterfaceInfo.setTotalNum(0L);
            userInterfaceInfo.setLeftNum(20L);
            userInterfaceInfoService.save(userInterfaceInfo);
            interfaceInfoLeftNumVO.setLeftNum(20L);
        }else {
            interfaceInfoLeftNumVO.setLeftNum(one.getLeftNum());
        }
        return interfaceInfoLeftNumVO;
    }
}




