package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.event.AnnotationEventListener;
import com.alec.InnovateX.spring.event.AppEvent;
import com.alec.InnovateX.spring.event.AsyncEventListener;
import com.alec.InnovateX.spring.event.BuiltinEventListener;
import com.alec.InnovateX.spring.event.EventConfig;
import com.alec.InnovateX.spring.event.OrderChangedEvent;
import com.alec.InnovateX.spring.event.OrderCreatedEvent;
import com.alec.InnovateX.spring.event.OrderPayload;
import com.alec.InnovateX.spring.event.OrderedListeners;
import com.alec.InnovateX.spring.event.UserChangedEvent;
import com.alec.InnovateX.spring.event.UserPayload;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑥事件机制进阶：@EventListener 注解式、泛型事件、@Order 顺序、
 * 同步多播的异常传播、@Async 异步事件
 */
public class EventAdvancedTest {

    @Test
    public void annotationListenerAndGenericEvent() {
        AnnotationEventListener.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EventConfig.class)) {
            ctx.publishEvent(new OrderCreatedEvent("SO-4001"));
            assertEquals(1, AnnotationEventListener.RECEIVED.size());

            // 泛型事件：OrderPayload 载荷只命中 OrderPayload 监听器
            ctx.publishEvent(new OrderChangedEvent(this, new OrderPayload("SO-4002", 99.9)));
            ctx.publishEvent(new UserChangedEvent(this, new UserPayload("alex", true)));
            assertEquals(3, AnnotationEventListener.RECEIVED.size());
            System.out.println("注解监听器 + 泛型事件命中: " + AnnotationEventListener.RECEIVED);
            assertTrue(AnnotationEventListener.RECEIVED.get(1).startsWith("EntityChangedEvent<OrderPayload>"));
            assertTrue(AnnotationEventListener.RECEIVED.get(2).startsWith("EntityChangedEvent<UserPayload>"));
        }
    }

    @Test
    public void listenerOrderAndExceptionPropagation() {
        OrderedListeners.EXECUTION.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EventConfig.class)) {
            // @Order(1) 先于 @Order(2)
            ctx.publishEvent(new OrderCreatedEvent("SO-4003"));
            assertEquals(2, OrderedListeners.EXECUTION.size());
            assertEquals("first", OrderedListeners.EXECUTION.get(0));
            assertEquals("second", OrderedListeners.EXECUTION.get(1));
            System.out.println("监听器执行顺序: " + OrderedListeners.EXECUTION);

            // 同步多播：前面的监听器抛异常 -> 后面的不再执行，异常传播回 publishEvent
            OrderedListeners.EXECUTION.clear();
            assertThrows(IllegalStateException.class,
                    () -> ctx.publishEvent(new com.alec.InnovateX.spring.event.RiskyEvent("risky")));
            assertEquals(1, OrderedListeners.EXECUTION.size());
            assertEquals("throwing", OrderedListeners.EXECUTION.get(0));
            System.out.println("异常中断后续监听器: 实际执行 " + OrderedListeners.EXECUTION);
        }
    }

    @Test
    public void builtinContextEvents() {
        BuiltinEventListener.EVENTS.clear();
        // 构造即 refresh -> ContextRefreshedEvent；start() -> ContextStartedEvent；close() -> ContextClosedEvent
        try (org.springframework.context.annotation.AnnotationConfigApplicationContext ctx =
                     new org.springframework.context.annotation.AnnotationConfigApplicationContext(EventConfig.class)) {
            assertEquals(java.util.List.of("refreshed"), BuiltinEventListener.EVENTS);
            ctx.start();
            assertEquals(java.util.List.of("refreshed", "started"), BuiltinEventListener.EVENTS);
        }
        assertEquals(java.util.List.of("refreshed", "started", "closed"), BuiltinEventListener.EVENTS);
        System.out.println("容器内置事件序列: " + BuiltinEventListener.EVENTS);
    }

    @Test
    public void xmlEventAndI18n() {
        // XML 版事件（接口式 ApplicationListener）+ ApplicationContext 的 i18n 消息解析
        try (org.springframework.context.support.GenericApplicationContext context = XmlContexts.load()) {
            context.publishEvent(new AppEvent(this, "spring event published"));
            // ResourceBundleMessageSource：basename=message，en_US 资源束里的精确值
            assertEquals("Original God", context.getMessage("app.message", null, "", java.util.Locale.US));
            System.out.println("XML 事件已发布；i18n(app.message, en_US) = Original God");
        }  // close 触发 AppContextClosedListener
    }

    @Test
    public void asyncListener() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EventConfig.class)) {
            ctx.publishEvent(new OrderCreatedEvent("SO-4004"));
            // 发布方立即返回，异步监听器在独立线程执行完
            assertTrue(AsyncEventListener.LATCH.await(3, TimeUnit.SECONDS));
            System.out.println("异步监听器线程: " + AsyncEventListener.threadName
                    + "，发布线程: " + Thread.currentThread().getName());
            assertNotEquals(Thread.currentThread().getName(), AsyncEventListener.threadName);
            assertTrue(AsyncEventListener.threadName.startsWith("event-async-"));
        }
    }
}
