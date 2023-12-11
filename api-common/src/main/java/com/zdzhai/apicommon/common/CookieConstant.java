package com.zdzhai.apicommon.common;

/**
 * @author dongdong
 * @Date 2023/4/15 10:37
 * cookie 过期时间
 */
public class CookieConstant {

    public static final String headAuthorization = "authorization";

    public static final String autoLoginAuthCheck = "api_remember";

    public static final int expireTime = 2592000 ;//30天过期

    public static final String orderToken = "api-order-token";

    public static final int orderTokenExpireTime = 1800; // 30分钟过期

    public static final byte[] autoLoginKey = "zzd-ApiAutoLogin".getBytes();

}
