package com.zdzhai.project.config;

import com.zdzhai.project.interceptor.AccessLimitInterceptor;
import com.zdzhai.project.interceptor.TokenVerifyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author dongdong
 * @Date 2023/4/15 20:00
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private TokenVerifyInterceptor tokenVerifyInterceptor;

    @Resource
    private AccessLimitInterceptor accessLimitInterceptor;

    /**
     * 放行接口
     */
    private List<String> pathPatterns = Arrays.asList("/userInterfaceInfo/updateUserLeftNum","/get/login","/user/captcha", "/user/register","/user/login","/user/loginBySms","/user/getCaptcha","/user/messageCaptcha","/v3/api-docs","/user/logoutSuccess","/user/getpassusertype","/user/sendPassUserCode","/user/authPassUserCode","/user/updateUserPass");

    /**
     * 放行静态资源
     */
    private List<String> staticPath = Arrays.asList("/charging/**","/swagger-ui.html", "/swagger-ui/*", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/webjars/**","/doc.html");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //这里不能直接new 拦截器，否则在拦截器中会出现无法注入service的问题
        registry.addInterceptor(tokenVerifyInterceptor)
                .excludePathPatterns(pathPatterns)
                .excludePathPatterns(staticPath).order(1);
        registry.addInterceptor(accessLimitInterceptor)
                .excludePathPatterns(pathPatterns)
                .excludePathPatterns(staticPath).order(2);
    }
}
