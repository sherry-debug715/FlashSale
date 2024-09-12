package com.flashsell.flashsell;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.flashsell.flashsell.util.RedisService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class RedisDemoTest {
    @Resource
    private RedisService redisService;

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

}
