package com.zdzhai.zdzhaiclientsdk;

import com.zdzhai.zdzhaiclientsdk.client.ZdzhaiApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongdong
 * @Date 2023/3/21 20:53
 */
@Data
@Configuration
@ConfigurationProperties("zdzhai.client")
public class ZdzhaiApiClientConfig {
    private String accessKey;

    private String secretKey;

    @Bean
    public ZdzhaiApiClient zdzhaiApiClient() {
        return new ZdzhaiApiClient(accessKey,secretKey);
    }
}
