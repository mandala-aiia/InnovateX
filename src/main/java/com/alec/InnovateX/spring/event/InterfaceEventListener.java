package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 接口式监听器：实现 ApplicationListener、按泛型类型接收事件。
 * 注意泛型上界是 ApplicationEvent——POJO 事件（如 OrderCreatedEvent）经 publishEvent
 * 发布时会被容器包装成 PayloadApplicationEvent&lt;T&gt;，接口式监听器要声明这个包装类型才能收到；
 * 而 @EventListener 方法直接声明 POJO 类型即可。
 * 接口式的剩余价值是可以实现 Ordered 控制同事件的监听顺序
 */
public class InterfaceEventListener implements ApplicationListener<PayloadApplicationEvent<OrderCreatedEvent>> {

    public static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<OrderCreatedEvent> event) {
        RECEIVED.add("interface:" + event.getPayload().getOrderNo());
        System.out.println("接口式监听器收到: " + event.getPayload().getOrderNo());
    }
}
