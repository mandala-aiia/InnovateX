package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationEvent;

/**
 * 泛型事件基类：继承 ApplicationEvent 并携带载荷泛型 T。
 *
 * 关键教学点——为什么不能直接 publish 裸的 EntityChangedEvent：泛型在字节码里已被擦除，
 * 多播器无从得知 T 是什么。正确姿势是"为每种载荷定义固化泛型的具体子类"：
 * ProductChangedEvent extends EntityChangedEvent&lt;ProductPayload&gt; 这样的子类把泛型实参写死在继承关系里，
 * ResolvableType 沿继承链能解析出 T 的具体类型，从而精确匹配监听器方法声明的
 * EntityChangedEvent&lt;ProductPayload&gt;（ProductPayload 的监听器不会误收 MemberPayload 的事件）。
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
