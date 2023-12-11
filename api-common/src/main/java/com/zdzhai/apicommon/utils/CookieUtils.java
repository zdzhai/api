package com.zdzhai.apicommon.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.zdzhai.apicommon.common.CookieConstant;

/**
 * cookie操作相关工具
 * @author dongdong
 */
public class CookieUtils {

    SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, CookieConstant.autoLoginKey);


    /**
     * 根据用户id和账号去生成记住登录的密钥
     * @param id
     * @param userAccount
     * @return
     */
    public String generateAutoLoginContent(String id , String userAccount){
        String res = id+ ":" + userAccount+ ":" + CookieConstant.autoLoginKey;
        return aes.encryptHex(res);
    }



    /**
     * 对remember-me的key进行解密
     * @param encryptHex
     * @return
     */
    public String[] decodeAutoLoginKey(String encryptHex){
        String decryptStr = aes.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        return decryptStr.split(":");
    }
}
