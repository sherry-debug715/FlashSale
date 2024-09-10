package com.flashsell.flashsell.db.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.flashsell.flashsell.db.mappers.SeckillActivityMapper;
import com.flashsell.flashsell.db.po.SeckillActivity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SeckillActivityDaoImpl implements SeckillActivityDao {

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Override
    public List<SeckillActivity> querySeckillActivitysByStatus(int activityStatus) {
        return seckillActivityMapper.querySeckillActivitysByStatus(activityStatus);
    }

    @Override
    public void insertSeckillActivity(SeckillActivity seckillActivity) {
        seckillActivityMapper.insert(seckillActivity);
    }

    @Override
    public SeckillActivity querySeckillActivityById(long activityId) {
        return seckillActivityMapper.selectByPrimaryKey(activityId);
    }

    @Override
    public void updateSeckillActivity(SeckillActivity seckillActivity) {
        seckillActivityMapper.updateByPrimaryKey(seckillActivity);
    }

    @Override
    public boolean deductStock(long activityId) {
        int result = seckillActivityMapper.deductStock(activityId);
        if (result < 1) {
            log.error("Stock deduction failed");
            return false;
        }
        return true;
    }

    @Override
    public boolean lockStock(long activityId) {
        int result = seckillActivityMapper.lockStock(activityId);
        if (result < 1) {
            log.error("Stock lock failed");
            return false;
        }
        return true;
    }
}

