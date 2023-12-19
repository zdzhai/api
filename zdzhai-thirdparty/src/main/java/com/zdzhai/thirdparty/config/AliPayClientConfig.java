package com.zdzhai.thirdparty.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 支付宝客户端
 * @author dongdong
 */
@Configuration
public class AliPayClientConfig {

    @Value("${alipay.CHARSET}")
    public String CHARSET ;

    @Value("${alipay.SIGN_TYPE}")
    public String SIGN_TYPE;

    @Value("${alipay.APP_ID}")
    private String APP_ID ;

    @Value("${alipay.PRIVATE_KEY}")
    public String PRIVATE_KEY;

    @Value("${alipay.ALIPAY_PUBLIC_KEY}")
    public String ALIPAY_PUBLIC_KEY;

    @Value("${alipay.ALIPAY_GATEWAY}")
    private String ALIPAY_GATEWAY ;

    @Value("${alipay.NotifyUrl}")
    public String NotifyUrl ;

    @Value("${alipay.tradeSuccessUrl}")
    public String tradeSuccessUrl ;

    public static String log_path = "D:\\";

    @Bean
    public AlipayClient alipayClient(){
        return new DefaultAlipayClient(ALIPAY_GATEWAY, APP_ID, PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
    }

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis() + ".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}