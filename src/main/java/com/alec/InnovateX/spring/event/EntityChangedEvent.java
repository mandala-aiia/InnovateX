package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationEvent;

/**
 * 泛型事件基类：同一个事件类按"载荷泛型 T"区分子类型。
 * 正确姿势：继承 ApplicationEvent，再为每种载荷定义"固化泛型的具体子类"
 * （如 OrderChangedEvent extends EntityChangedEvent<OrderPayload>）。
 * 多播器用 ResolvableType 解析具体子类的泛型实参，才能精确匹配
 * 声明了 EntityChangedEvent&lt;OrderPayload&gt; 参数的监听器——
 * 直接 publish 裸的 EntityChangedEvent（泛型已擦除）是匹配不到的
 */
public class EntityChangedEvent<T> extends ApplicationEvent {

    private final T payload;

    public EntityChangedEvent(Object source, T payload) {
        super(source);
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }
}
