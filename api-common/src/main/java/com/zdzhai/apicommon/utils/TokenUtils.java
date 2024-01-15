package com.zdzhai.apicommon.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongdong
 * @Date 2023/4/15 9:48
 */
@Slf4j
public class TokenUtils {

    /**
     * 密钥
     */
    private final byte[] data = "ZDZHAI.API".getBytes();
    /**
     * 创建签名器
     */
    private final JWTSigner signer = JWTSignerUtil.hs512(data);

    /**
     * 生成token
     * jwt一共由三部分
     * 标头（Header）
     * 有效载荷（Payload）
     * 签名（Signature）
     *
     * @param id
     * @param userAccount
     * @return
     */
    public String createToken(String id, String userAccount) {
        DateTime now = DateTime.now();
        DateTime expireTime = now.offsetNew(DateField.HOUR, 720);
        //jwt的payload部分
        Map<String, Object> payload = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("id", id);
                put("userAccount", userAccount);
                //签发时间
                put(RegisteredPayload.ISSUED_AT, now);
                //过期时间
                put(RegisteredPayload.EXPIRES_AT, expireTime);
            }
        };
        return JWTUtil.createToken(payload, signer);
    }


    /**
     * 验证token是否正确
     *
     * @param token
     * @return
     */
    public boolean verifyToken(String token) {
        try {
            final JWT jwt = JWTUtil.parseToken(token);
            String algorithm = jwt.getAlgorithm();
            if (algorithm == null || !"HS512".equals(algorithm)) {
                return false;
            }
            boolean verify = jwt.setSigner(signer).verify();
            if (!verify) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("verifyToken--->{}", String.valueOf(e));
            return false;
        }
    }

    /**
     * 验证token是否过期
     *
     * @param token
     * @return
     */
    public boolean verifyTokenTime(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        boolean verifyTime = jwt.validate(0);
        if (verifyTime) {
            return false;
        }
        return true;
    }

    /**
     * 重建token
     *
     * @param token
     * @return
     */
    public String reCreatToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        String userAccount = (String) jwt.getPayload("userAccount");
        String id = (String) jwt.getPayload("id");
        return createToken(id, userAccount);
    }
}
