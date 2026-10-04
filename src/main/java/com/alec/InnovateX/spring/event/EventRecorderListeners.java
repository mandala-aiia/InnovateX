package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @EventListener 注解式监听器：按"方法参数类型"匹配事件，一个类里集中监听多种事件——
 * 这是接口式 ApplicationListener 的现代替代写法（接口式一个类只能绑定一种事件类型）。
 *
 * 三个方法分别演示：POJO 事件直接声明 POJO 类型（框架自动拆掉 PayloadApplicationEvent 包装）、
 * 两个泛型事件靠 ResolvableType 精确区分泛型实参。
 */
public class EventRecorderListeners {

    public static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

    @EventListener
    public void onProductAdded(ProductAddedEvent event) {
        RECEIVED.add("product-added:" + event.getSku());
        System.out.println("[EventRecorderListeners] POJO 事件 ProductAddedEvent: " + event.getSku());
    }

    /** 只收 EntityChangedEvent<ProductPayload>；MemberPayload 载荷的事件进不来 */
    @EventListener
    public void onProductChanged(EntityChangedEvent<ProductPayload> event) {
        RECEIVED.add("product-changed:" + event.getPayload().getSku());
        System.out.println("[EventRecorderListeners] 泛型事件 EntityChangedEvent<ProductPayload>: sku="
                + event.getPayload().getSku() + ", price=" + event.getPayload().getPrice());
    }

    /** 只收 EntityChangedEvent<MemberPayload>；ProductPayload 载荷的事件进不来 */
    @EventListener
    public void onMemberChanged(EntityChangedEvent<MemberPayload> event) {
        RECEIVED.add("member-changed:" + event.getPayload().getUsername());
        System.out.println("[EventRecorderListeners] 泛型事件 EntityChangedEvent<MemberPayload>: username="
                + event.getPayload().getUsername() + ", active=" + event.getPayload().isActive());
    }
}
