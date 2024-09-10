package com.flashsell.flashsell.db.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.flashsell.flashsell.db.mappers.OrderMapper;
import com.flashsell.flashsell.db.po.Order;



@Repository
public class OrderDaoImpl implements OrderDao{
    
    @Resource
    private OrderMapper orderMapper;

    @Override
    public void insertOrder(Order order) {
        orderMapper.insert(order);
    }

    @Override
    public Order queryOrder(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public void updateOrder(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }
}
