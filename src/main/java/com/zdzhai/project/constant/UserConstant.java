package com.zdzhai.project.constant;

/**
 * 用户常量
 *
 * @author dongdong
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 用户信息的redisKEY
     */
    String API_USER_ID = "api:user:";

    /**
     * 验证码的redisKEY
     */
    String API_CAPTCHA_ID = "api:captchaId:";

    /**
     * 系统用户 id（虚拟用户）
     */
    long SYSTEM_USER_ID = 0;

    //  region 权限

    /**
     * 默认权限
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员权限
     */
    String ADMIN_ROLE = "admin";

    // endregion
}
