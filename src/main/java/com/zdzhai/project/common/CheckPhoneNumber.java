package com.zdzhai.project.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dongdong
 * @Date 2023/4/16 11:02
 * 校验手机号和图形验证码
 */
public class CheckPhoneNumber {

    /**
     * 验证手机号是否符合要求
     * @param mobile
     * @return
     */
    public boolean isPhoneNum(String mobile){
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(16[5,6])|(17[0-8])|(18[0-9])|(19[1、5、8、9]))\\d{8}$";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(mobile);
        return m.matches();
    }
    /**
     * 验证输入的图形验证码是否正确
     * @param captcha
     * @return
     */
    public boolean isCaptcha(String captcha){
        String regex = "/^[0-9]\\d{3}$/";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(captcha);
        return m.matches();
    }
}
