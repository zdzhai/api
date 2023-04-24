package com.zdzhai.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.Jitang;


import com.zdzhai.project.mapper.JitangMapper;
import com.zdzhai.project.service.JitangService;
import org.springframework.stereotype.Service;

/**
* @author 62618
* @description 针对表【jitang(鸡汤)】的数据库操作Service实现
* @createDate 2023-04-21 21:53:39
*/
@Service
public class JitangServiceImpl extends ServiceImpl<JitangMapper, Jitang>
    implements JitangService{
    @Override
    public void validJitang(Jitang jitang, boolean add) {
        if (jitang == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (jitang.getId() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}




