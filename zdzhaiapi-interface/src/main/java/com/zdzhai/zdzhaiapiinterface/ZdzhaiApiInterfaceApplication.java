package com.zdzhai.zdzhaiapiinterface;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dongdong
 * @Date 2023/3/20 22:31
 */

//@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@SpringBootApplication
@MapperScan("com.zdzhai.zdzhaiapiinterface.mapper")
public class ZdzhaiApiInterfaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZdzhaiApiInterfaceApplication.class, args);
    }
}
