package com.flashsell.flashsell.component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.po.SeckillActivity;
import com.flashsell.flashsell.util.RedisService;

@Component
public class RedisPreheatRunner implements ApplicationRunner {
    @Autowired
    RedisService redisService;

    @Autowired
    SeckillActivityDao seckillActivityDao;

    /**
    * update redis with stocks when application starts
    * @param args
    * @throws Exception
    */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
        for (SeckillActivity seckillActivity : seckillActivities) {
            redisService.setValue("stock:" + seckillActivity.getId(), (long) seckillActivity.getAvailableStock());
        }
    }
    
}
