package com.alec.InnovateX.spring.aop;

public class AopOrderServiceImpl implements AopOrderService {

    @Override
    public String createOrder(String orderNo) {
        System.out.println("[AopOrderServiceImpl] createOrder: " + orderNo);
        return "订单已创建: " + orderNo;
    }

    @Override
    public String cancelOrder(String orderNo) {
        System.out.println("[AopOrderServiceImpl] cancelOrder: " + orderNo);
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        return "订单已取消: " + orderNo;
    }
}
