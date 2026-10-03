package com.alec.InnovateX.spring.event;

/** 固化泛型的具体子类：ResolvableType 能解析出 T = OrderPayload */
public class OrderChangedEvent extends EntityChangedEvent<OrderPayload> {

    public OrderChangedEvent(Object source, OrderPayload payload) {
        super(source, payload);
    }
}
