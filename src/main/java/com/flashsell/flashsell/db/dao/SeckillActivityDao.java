package com.flashsell.flashsell.db.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.flashsell.flashsell.db.po.SeckillActivity;

@Mapper
public interface SeckillActivityDao {
    public List<SeckillActivity> querySeckillActivitysByStatus(int activityStatus);

    public void insertSeckillActivity(SeckillActivity seckillActivity);

    public SeckillActivity querySeckillActivityById(long activityId);

    public void updateSeckillActivity(SeckillActivity seckillActivity);
    
    public boolean deductStock(long activityId);

    public boolean lockStock(long activityId);

    void revertStock(Long seckillActivityId);
}
