package com.zdzhai.order.config;

import com.google.gson.Gson;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.constant.OrderInfoConstant;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.dto.AliPayDtoMQ;
import com.zdzhai.apicommon.model.entity.ApiOrder;
import com.zdzhai.order.service.ApiOrderService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * RocketMQ配置类
 *
 * @author dongdong
 * @Date 2023/12/24 13:13
 */
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name.server.addr}")
    public String nameServerAddr;

    @Resource
    private ApiOrderService apiOrderService;


    /**
     * 延迟订单生产者
     *
     * @return
     * @throws MQClientException
     */
    @Bean("MQProducer")
    public DefaultMQProducer ordersProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(OrderInfoConstant.GROUP_ORDERS);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    /**
     * 延迟订单消费者
     * 采用推送模式的消费者及监听器
     *
     * @return
     * @throws MQClientException
     */
    @Bean("MQConsumer")
    public DefaultMQPushConsumer ordersConsumer() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(OrderInfoConstant.GROUP_ORDERS);
        consumer.setNamesrvAddr(nameServerAddr);
        try {
            consumer.subscribe(OrderInfoConstant.TOPIC_ORDERS, "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                try {
                    MessageExt msg = msgs.get(0);
                    if (msg == null) {
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    String bodyStr = new String(msg.getBody());
                    Gson gson = new Gson();
                    ApiOrder apiOrder = gson.fromJson(bodyStr, ApiOrder.class);
                    //orderSn
                    String orderSn = apiOrder.getOrderSn();
                    System.out.println("时间:"+ System.currentTimeMillis()+";延迟下单消息消费者已接收到消息-topic={"+msg.getTopic()+"}, 消息内容={"+orderSn+"}");
                    //是否在支付成功后，删除订单号对应的延迟队列，
                    //应该也不需要处理，在消费延迟队列的时候对支付状态进行判断，未支付的删掉，已支付的不用管。
                    //根据订单号查订单表，查看订单的支付状态 0->待付款；1->已完成；2->无效订单
                    ApiOrder dbApiOrder = apiOrderService.getApiOrderByOrderSn(orderSn);
                    if (dbApiOrder != null && dbApiOrder.getStatus() == 0) {
                        int update = apiOrderService.updateApiOrderStatusByOrderSn(orderSn, 2);
                        if (update <= 0) {
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch (MQClientException e) {
            //错误信息
            e.printStackTrace();
        }
        return consumer;
    }


    /**
     * 订单支付成功消费者
     * 采用推送模式的消费者及监听器
     *
     * @return
     * @throws MQClientException
     */
    @Bean("MQOrderSuccessConsumer")
    public DefaultMQPushConsumer orderSuccessConsumer() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(OrderInfoConstant.GROUP_SUCCESS_ORDER);
        consumer.setNamesrvAddr(nameServerAddr);
        try {
            consumer.subscribe(OrderInfoConstant.TOPIC_SUCCESS_ORDER, "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                try {
                    MessageExt msg = msgs.get(0);
                    if (msg == null) {
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    String bodyStr = new String(msg.getBody());
                    Gson gson = new Gson();
                    //orderSn
                    AliPayDtoMQ alipayInfoMQ = gson.fromJson(bodyStr, AliPayDtoMQ.class);
                    String orderSn = alipayInfoMQ.getOrderSn();
                    String tradeStatus = alipayInfoMQ.getTradeStatus();
                    System.out.println("时间:"+ System.currentTimeMillis()+";订单支付成功消费者已接收到消息-topic={"+msg.getTopic()+"}, 消息内容={"+orderSn+"}");
                    //消费订单成功队列消息，修改订单表订单为已支付状态 0->待付款；1->已完成；2->无效订单
                    if ("TRADE_SUCCESS".equals(tradeStatus)) {
                        int update = apiOrderService.updateApiOrderStatusByOrderSn(orderSn, 1);
                        if (update <= 0) {
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch (MQClientException e) {
            //错误信息
            e.printStackTrace();
        }
        return consumer;
    }
}
