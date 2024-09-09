package com.flashsell.flashsell;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.flashsell.flashsell.util.RedisService;

@SpringBootTest
public class RedisDemoTest {
    @Resource
    private RedisService redisService;

    @Test
    public void stockTest() {
        redisService.setValue("stock:19",10L);
    }

    @Test 
    public void getStockTest() {
        String stock = redisService.getValue("stock:19");
        System.out.println(stock);
    }

    @Test
    public void stockDeductValidatorTest() {
        boolean res = redisService.stockDeductValidator(("stock:19"));
        System.out.println("res: " + res);
        String stock = redisService.getValue("stock:19");
        System.out.println("stock: " + stock);
        
    }

}
