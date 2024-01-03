package com.zdzhai.order.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author dongdong
 * @Date 2024/1/1 17:01
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedisIdWorkerTest {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    void nextOrderSn() {
        System.out.println(redisIdWorker.nextOrderSn("api-order"));
    }
}