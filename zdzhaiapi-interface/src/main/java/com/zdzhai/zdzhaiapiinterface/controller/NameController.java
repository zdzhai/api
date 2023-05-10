package com.zdzhai.zdzhaiapiinterface.controller;

import com.zdzhai.apicommon.model.entity.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * api接口类
 * @author dongdong
 * @Date 2023/3/20 22:30
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(String name, HttpServletRequest request){
        System.out.println(request.getHeader("zdzhai"));
        return "Get 你的名字是" + name;
    }


    @PostMapping("/post")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request){
        String res = "用户的名字是" + user.getUserName();
        System.out.println(res);
        return res;
    }
}
