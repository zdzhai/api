package com.zdzhai.project.service;

import com.zdzhai.apicommon.model.entity.Jitang;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.project.model.entity.Post;

/**
* @author 62618
* @description 针对表【jitang(鸡汤)】的数据库操作Service
* @createDate 2023-04-21 21:53:39
*/

public interface JitangService extends IService<Jitang> {

    /**
     * 校验
     *
     * @param jitang
     * @param add 是否为创建校验
     */
    void validJitang(Jitang jitang, boolean add);
}
