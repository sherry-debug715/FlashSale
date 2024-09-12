package com.flashsell.flashsell.mq;

import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.po.Order;

import lombok.extern.slf4j.Slf4j;

/**
 * Payment completed
 * Deduct stock from mysql
 */
@Slf4j
@Component
@Transactional
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Override
    public void onMessage(MessageExt messageExt) {
        // 1. parse create order request message
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received create order request message: " + message);
        Order order = JSON.parseObject(message, Order.class);
        //2. Deduct stock from mysql
        seckillActivityDao.deductStock(order.getSeckillActivityId());
    }
}
