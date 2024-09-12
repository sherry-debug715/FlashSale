package com.flashsell.flashsell.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.flashsell.flashsell.db.dao.OrderDao;
import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.po.Order;
import com.flashsell.flashsell.db.po.SeckillActivity;
import com.flashsell.flashsell.mq.RocketMQService;
import com.flashsell.flashsell.util.RedisService;
import com.flashsell.flashsell.util.SnowFlake;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SeckillActivityService {
    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    OrderDao orderDao;

    /**
    * datacenterId; 数据中心 (Data center)
    * machineId; 机器标识 (Machine ID)
    * 在分布式环境中可以从机器配置上读取 (In a distributed environment, it can be read from machine configurations)
    * 单机开发环境中先写死 (For local development, it's hard-coded)
    */
    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /*
     * Create order
     * @param seckillActivityId
     * @param userId
     * @return
     * @throws Exception
     */
    public Order createOrder(long seckillActivityId, long userId) throws Exception {
        /*
         * 1. create order
         */
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        // SnowFlake algorithmn to generate order number 
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        /*
         * 2. deliver newly created order message
         */
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));
        /*
         * 3. message delay
         * delayTimeLevel is set to 3 meaning set level to 3, 10s
         */
        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order), 3);
        return order;
    }

    /*
     * Check whether there are more stocks
     * @param activityId 
     * @return
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }

    /*
    * Order payment complete
    * @param orderNo
    */
    public void payOrderProcess(String orderNo) throws Exception {
        log.info("Completed payment, order number: " + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        /*
        * 1.Check if order exist
        * 2.Check if order is paid */
        if (order == null) { 
            log.error("Order with order number: " + orderNo + " doesn't exist"); 
            return;
        } else if (order.getOrderStatus() != 1 ) { 
            log.error("Invalid order: " + orderNo); 
            return;
        }
        /*
        * 2.Order paid */
        order.setPayTime(new Date());
        // Order status: 0: Out of stock, invalid order; 1: order created, waiting for payment; 2: Payment completed.
        orderDao.updateOrder(order);
        /*
        *3.Send order payment completed message
        */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }
}
