package com.zdzhai.gateway;

import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.service.InnerUserService;
import com.zdzhai.project.provider.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author dongdong
 * @Date 2023/3/25 16:53
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@Slf4j
@EnableDubbo
@Service
public class ZdzhaiGatewayApplication {

    @DubboReference
    private DemoService demoService;


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ZdzhaiGatewayApplication.class);
        ZdzhaiGatewayApplication application = context.getBean(ZdzhaiGatewayApplication.class);
        String result = application.doSayHello("world");
        String result2 = application.doSayHello2("world");
        System.out.println("result: " + result);
        System.out.println("result: " + result2);
    }
    public String doSayHello(String name) {
        return demoService.sayHello(name);
    }

    public String doSayHello2(String name) {
        return demoService.sayHello2(name);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("tobaidu", r -> r.path("/baidu")
                        .uri("http://baidu.com"))
                .route("tozhaiico", r -> r.host("*.myhost.org")
                        .uri("http://httpbin.org"))
                .build();
    }

}
