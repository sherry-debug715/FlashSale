package com.flashsell.flashsell;

import javax.annotation.Resource;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.flashsell.flashsell.db.mappers.SeckillActivityMapper;
import com.flashsell.flashsell.services.SeckillActivityService;
import com.flashsell.flashsell.util.RedisService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class RedisDemoTest {
    @Resource
    private RedisService redisService;

    @Autowired
    SeckillActivityService seckillActivityService;

    @Test
    public void stockTest() {
        log.info("Setting stock value...");
        redisService.setValue("stock:19",10L);
        log.info("Stock value set.");
    }

    @Test 
    public void getStockTest() {
        String stock = redisService.getValue("stock:19");
        log.info ("stock 19 value" + stock);
    }

    @Test
    public void stockDeductValidatorTest() {
        boolean res = redisService.stockDeductValidator(("stock:19"));
        System.out.println("res: " + res);
        String stock = redisService.getValue("stock:19");
        System.out.println("stock: " + stock);
        
    }

    @Test 
    public void revertStock() {
        String stock = redisService.getValue("stock:19");
        System.out.println("回滚库存之前的库存：" + stock);

        redisService.revertStock("stock:19");

        stock = redisService.getValue("stock:19");
        System.out.println("回滚库存之后的库存：" + stock);
    }

    @Test
    public void pushSeckillInfoToRedisTest() {
        seckillActivityService.pushSeckillInfoToRedis(19);
    }

    @Test
    public void removeLimitMemberTest() {
        redisService.removeLimitMember(19L, 1234L);
    }

    /**
    * get lock key under high concurrency
    */
    @Test
    public void  testConcurrentAddLock() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // Log result: true false false false false false false false false false 
            // only the first request can get the key
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
        }
    }

    /** 
    * Under high concurrency, unlock as soon as a key is locked
    */
    @Test
    public void  testConcurrent() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // log result: true true true true true true true true true true 
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
            redisService.releaseDistributedLock("A", requestId);
        } 
    }

}
