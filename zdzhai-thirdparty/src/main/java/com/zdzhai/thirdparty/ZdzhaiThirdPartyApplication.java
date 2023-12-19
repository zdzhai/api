package com.zdzhai.thirdparty;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author dongdong
 * @Date 2023/12/4 15:09
 * 第三方服务，如：gitee 、github等
 */
@EnableDubbo
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ZdzhaiThirdPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZdzhaiThirdPartyApplication.class, args);
    }
}
