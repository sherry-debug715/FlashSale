package com.flashsell.flashsell.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flashsell.flashsell.util.RedisService;

@Service
public class SeckillActivityService {
    @Autowired
    private RedisService redisService;

    /*
     * Check whether there are more stocks
     * @param activityId 
     * @return
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }
}
