package com.zdzhai.zdzhaiapiinterface.controller;

import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.Jitang;
import com.zdzhai.apicommon.model.entity.Tuwei;
import com.zdzhai.zdzhaiapiinterface.service.JitangService;
import com.zdzhai.zdzhaiapiinterface.service.TuweiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author dongdong
 * @Date 2023/4/21 23:24
 * 常见的请求接口的方法
 */
@RestController
@RequestMapping("/get")
public class GetController {

    @Resource
    private JitangService jitangService;

    @Resource
    private TuweiService tuweiService;


    @GetMapping("/jitang")
    public String getJitang(String id){

        Jitang jitang = jitangService.getById(id);
        if (jitang == null || jitang.getSentence() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return jitang.getSentence();
    }

    @GetMapping("/tuwei")
    public String getTuwei(String id){

        Tuwei tuwei = tuweiService.getById(id);
        if (tuwei == null || tuwei.getSentence() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return tuwei.getSentence();
    }

}
