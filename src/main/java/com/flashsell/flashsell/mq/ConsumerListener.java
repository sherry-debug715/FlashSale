package com.flashsell.flashsell.mq;

import java.io.UnsupportedEncodingException;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "test-flashsale", consumerGroup = "comsumerGroup-flashsale")
public class ConsumerListener implements RocketMQListener<MessageExt> {
    
    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            String body = new String(messageExt.getBody(), "UTF-8");
            System.out.println("receive message:" + body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
