package com.flashsell.flashsell.mq;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.flashsell.flashsell.db.dao.OrderDao;
import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.po.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RocketMQMessageListener(topic = "seckill_order", consumerGroup = "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        //1. Parse create new order request
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Receive create new order request" + message);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(new Date());
        //2.deduct stock 
        boolean lockStockResult = seckillActivityDao.lockStock(order.getSeckillActivityId());
        if (lockStockResult) {
            //订单状态 0:没有可用库存，无效订单 1:已创建等待付款
            // Order status: 0: out of stock, invalid order; 1: Order created, ready to proceed to payment.
            order.setOrderStatus(1);
        } else {
            order.setOrderStatus(0);
        }
        //3.Insert order
        orderDao.insertOrder(order);
    }
}
