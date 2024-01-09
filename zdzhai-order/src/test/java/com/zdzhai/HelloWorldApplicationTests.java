package com.zdzhai;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author dongdong
 * @Date 2024/1/4 16:37
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
classes = {LoggerFactory.class})
public class HelloWorldApplicationTests {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void logTest() {
        logger.trace("trace级别日志");
        logger.debug("debug级别日志");
        logger.info("info级别日志");
        logger.warn("Warn级别日志");
        logger.error("error级别日志");
    }
}
