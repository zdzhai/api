package com.zdzhai.zdzhaiapiinterface.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.Tuwei;

import com.zdzhai.zdzhaiapiinterface.mapper.TuweiMapper;
import com.zdzhai.zdzhaiapiinterface.service.TuweiService;
import org.springframework.stereotype.Service;

/**
* @author 62618
* @description 针对表【tuwei(土味情话)】的数据库操作Service实现
* @createDate 2023-04-21 21:55:32
*/
@Service
public class TuweiServiceImpl extends ServiceImpl<TuweiMapper, Tuwei>
    implements TuweiService {
    @Override
    public void validTuwei(Tuwei tuwei, boolean add) {
        if (tuwei == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (tuwei.getId() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}




