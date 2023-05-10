package com.zdzhai.zdzhaiapiinterface;

import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.zdzhaiclientsdk.client.ZdzhaiApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author dongdong
 * @Date 2023/3/21 21:10
 */
@SpringBootTest
public class ZdzhaiApiInterfaceTests {

    @Resource
    private ZdzhaiApiClient zdzhaiApiClient;

    @Test
    void contestLoads(){
        String result1 = zdzhaiApiClient.getNameByGet("dongdong");
        String result2 = zdzhaiApiClient.getNameByPost(new User("dongdong"));
        System.out.println(result1);
        System.out.println(result2);
    }
}
