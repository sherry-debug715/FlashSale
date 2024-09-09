package com.flashsell.flashsell.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.po.SeckillActivity;

@Service
public class SeckillOverSellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    public String processSeckill(long activityId) {
        SeckillActivity seckillActivity =
        seckillActivityDao.querySeckillActivityById(activityId);
        long availableStock = seckillActivity.getAvailableStock();
        String result;
        if (availableStock > 0) {
            result = "Congratulations, the flash sale was successful.";
        System.out.println(result);
        availableStock = availableStock - 1;
        seckillActivity.setAvailableStock(new Integer("" + availableStock));
        seckillActivityDao.updateSeckillActivity(seckillActivity);
        } else {
        result = "Sorry, the flash sale failed, the item has been sold out.";
        System.out.println(result);
        }
        return result;
    }
}
