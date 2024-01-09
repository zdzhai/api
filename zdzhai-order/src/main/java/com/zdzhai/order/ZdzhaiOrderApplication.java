package com.zdzhai.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author dongdong
 * @Date 2023/12/26 19:15
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableDubbo
@SpringBootApplication
public class ZdzhaiOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZdzhaiOrderApplication.class, args);
    }
}
