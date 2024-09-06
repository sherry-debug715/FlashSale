package com.flashsell.flashsell.db.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.flashsell.flashsell.db.mappers.SeckillCommodityMapper;
import com.flashsell.flashsell.db.po.SeckillCommodity;

@Repository
public class SeckillCommodityDaoImpl implements SeckillCommodityDao {

    @Resource
    private SeckillCommodityMapper seckillCommodityMapper;

    @Override
    public SeckillCommodity querySeckillCommodityById(long commodityId) {
        return seckillCommodityMapper.selectByPrimaryKey(commodityId);
    }
}
