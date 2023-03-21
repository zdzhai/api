package com.zdzhai.zdzhaiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * 签名工具类
 * @author dongdong
 * @Date 2023/3/21 20:32
 */
public class SignUtil {
    /**
     *
     * @param body 用户参数json
     * @param secretKey 密钥
     * @return 不可解密的值
     */
    public static String getSign(String body, String secretKey) {
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String context = body + '.' + secretKey;
        return md5.digestHex(context);
    }
}
