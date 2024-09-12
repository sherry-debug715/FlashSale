package com.flashsell.flashsell.mq;

import java.nio.charset.StandardCharsets;

import javax.annotation.Resource;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.flashsell.flashsell.db.dao.OrderDao;
import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.po.Order;
import com.flashsell.flashsell.util.RedisService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao  orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received order payment status verification message ===>" + message);
        Order order = JSON.parseObject(message, Order.class);
        //1. Check order
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());
        if (orderInfo == null) {
            log.info("Order of order number: " + order.getOrderNo() + " doesn't exist.");
            return;
        }
        //2. Check if payment was made
        // status 2: payment made
        if (orderInfo.getOrderStatus() != 2) {
            //3.Payment not completed, close the order
            log.info("Payment not completed, order number: "+orderInfo.getOrderNo());
            // status 99: close order
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);
            // 4: 1) revert stock in mySql
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // 4: 2) revert stock in redis 
            redisService.revertStock("stock:" + order.getSeckillActivityId());
            // 5: remove user 
            redisService.removeLimitMember(order.getSeckillActivityId(), order.getUserId());
        }
    }
}

