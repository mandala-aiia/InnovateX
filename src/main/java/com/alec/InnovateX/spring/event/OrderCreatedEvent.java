package com.alec.InnovateX.spring.event;

/** 普通事件对象：Spring 4.2 之后不再强制继承 ApplicationEvent，任意 POJO 都能当事件 */
public class OrderCreatedEvent {

    private final String orderNo;

    public OrderCreatedEvent(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }
}
