package com.flashsell.flashsell.db.dao;

import java.util.List;

import com.flashsell.flashsell.db.po.SeckillActivity;

public interface SeckillActivityDao {
    public List<SeckillActivity> querySeckillActivitysByStatus(int activityStatus);

    public void inertSeckillActivity(SeckillActivity seckillActivity);

    public SeckillActivity querySeckillActivityById(long activityId);

    public void updateSeckillActivity(SeckillActivity seckillActivity);
}
