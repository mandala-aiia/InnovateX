package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @EventListener 注解式监听器（对比现有 spring 包的 ApplicationListener 接口式写法）：
 * 按方法参数类型匹配事件，一个类里可以集中监听多种事件
 */
@Component
public class AnnotationEventListener {

    public static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        RECEIVED.add("OrderCreatedEvent: " + event.getOrderNo());
        System.out.println("[AnnotationEventListener] 收到订单事件: " + event.getOrderNo());
    }

    /** 泛型事件：只接收 EntityChangedEvent<OrderPayload>，UserPayload 载荷的事件不会进来 */
    @EventListener
    public void onOrderChanged(EntityChangedEvent<OrderPayload> event) {
        RECEIVED.add("EntityChangedEvent<OrderPayload>: " + event.getPayload().orderNo());
        System.out.println("[AnnotationEventListener] 收到订单泛型事件: " + event.getPayload());
    }

    @EventListener
    public void onUserChanged(EntityChangedEvent<UserPayload> event) {
        RECEIVED.add("EntityChangedEvent<UserPayload>: " + event.getPayload().username());
        System.out.println("[AnnotationEventListener] 收到用户泛型事件: " + event.getPayload());
    }
}
