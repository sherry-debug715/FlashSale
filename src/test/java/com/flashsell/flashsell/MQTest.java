package com.flashsell.flashsell;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.flashsell.flashsell.mq.RocketMQService;

@SpringBootTest
public class MQTest {
    @Autowired
    RocketMQService rocketMQService;

    @Test
    public void sendMQTest() throws Exception {
        rocketMQService.sendMessage("test-flashsale", "hello world, this is working" + new Date().toString());
    }
}
