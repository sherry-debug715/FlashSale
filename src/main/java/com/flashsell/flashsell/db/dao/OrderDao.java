package com.flashsell.flashsell.db.dao;

import com.flashsell.flashsell.db.po.Order;

public interface OrderDao {
    void insertOrder(Order order);
    Order queryOrder(String orderNo);
    void updateOrder(Order order);
}