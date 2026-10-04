package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 接口式监听器（XML 时代唯一写法的注解装配版）——本类是"POJO 事件包装规则"的教学载体：
 *
 * ApplicationListener 的泛型上界是 ApplicationEvent（&lt;E extends ApplicationEvent&gt;）。
 * POJO 事件 ProductAddedEvent 并不是 ApplicationEvent，publishEvent 发布时容器会把它
 * 包装成 PayloadApplicationEvent&lt;ProductAddedEvent&gt; 再多播——
 * 所以接口式监听器必须声明"包装类型 PayloadApplicationEvent&lt;ProductAddedEvent&gt;"才收得到；
 * 而 @EventListener 方法直接声明 POJO 类型即可（框架发现监听的是载荷类型会自动拆包）。
 *
 * 接口式的剩余价值：可以实现 Ordered 接口精细控制同事件监听顺序（注解式用 @Order 更直观）。
 */
public class LegacyInterfaceListener implements ApplicationListener<PayloadApplicationEvent<ProductAddedEvent>> {

    public static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<ProductAddedEvent> event) {
        RECEIVED.add("interface:" + event.getPayload().getSku());
        System.out.println("[LegacyInterfaceListener] 接口式收到包装事件 PayloadApplicationEvent, 载荷 sku="
                + event.getPayload().getSku());
    }
}
