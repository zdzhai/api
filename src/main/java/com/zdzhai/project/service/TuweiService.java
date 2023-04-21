package com.zdzhai.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.model.entity.Tuwei;

/**
* @author 62618
* @description 针对表【tuwei(土味情话)】的数据库操作Service
* @createDate 2023-04-21 21:55:32
*/
public interface TuweiService extends IService<Tuwei> {

    /**
     * 校验
     *
     * @param tuwei
     * @param add 是否为创建校验
     */
    void validTuwei(Tuwei tuwei, boolean add);
}
