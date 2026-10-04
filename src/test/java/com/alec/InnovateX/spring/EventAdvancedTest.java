package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.event.AsyncEventConfig;
import com.alec.InnovateX.spring.event.AuditAlarmEvent;
import com.alec.InnovateX.spring.event.CoreEventConfig;
import com.alec.InnovateX.spring.event.ContextLifecycleRecorder;
import com.alec.InnovateX.spring.event.EventRecorderListeners;
import com.alec.InnovateX.spring.event.InventoryService;
import com.alec.InnovateX.spring.event.LegacyInterfaceListener;
import com.alec.InnovateX.spring.event.LifecycleEventConfig;
import com.alec.InnovateX.spring.event.MemberChangedEvent;
import com.alec.InnovateX.spring.event.MemberPayload;
import com.alec.InnovateX.spring.event.OrderingEventConfig;
import com.alec.InnovateX.spring.event.ShipmentAsyncListener;
import com.alec.InnovateX.spring.event.ShipmentDispatchedEvent;
import com.alec.InnovateX.spring.event.StockEventListeners;
import com.alec.InnovateX.spring.event.StockMovedEvent;
import com.alec.InnovateX.spring.event.ThresholdAlertListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 事件机制进阶：@EventListener 注解式（一个类集中监听多事件）、接口式 ApplicationListener 与
 * PayloadApplicationEvent 包装规则、泛型事件 ResolvableType 精确匹配、@Order 监听顺序、
 * 同步多播异常中断传播、@Async 异步监听（自定义 executor 线程名断言）、容器内置事件、MessageSource i18n。
 */
public class EventAdvancedTest {

    @Test
    public void annotationListenerAndGenericEvent() {
        EventRecorderListeners.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CoreEventConfig.class)) {
            // 业务 Bean 经注入的 ApplicationEventPublisher 发布：一次业务动作发两个事件
            ctx.getBean(InventoryService.class).addProduct("SKU-7001", 19.9);
            assertEquals(2, EventRecorderListeners.RECEIVED.size());

            // 泛型事件：固化泛型的具体子类直接 publish，ResolvableType 按泛型实参精确匹配、互不串扰
            ctx.publishEvent(new MemberChangedEvent(this, new MemberPayload("alex", true)));
            assertEquals(3, EventRecorderListeners.RECEIVED.size());

            assertEquals("product-added:SKU-7001", EventRecorderListeners.RECEIVED.get(0));
            assertEquals("product-changed:SKU-7001", EventRecorderListeners.RECEIVED.get(1));
            assertEquals("member-changed:alex", EventRecorderListeners.RECEIVED.get(2));
            System.out.println("注解式监听器 + 泛型事件命中: " + EventRecorderListeners.RECEIVED);
        }
    }

    @Test
    public void interfaceListenerReceivesPayloadWrapper() {
        LegacyInterfaceListener.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CoreEventConfig.class)) {
            ctx.getBean(InventoryService.class).addProduct("SKU-7002", 29.9);
            // POJO 事件被包装成 PayloadApplicationEvent<ProductAddedEvent> 多播——接口式声明的是包装类型
            assertEquals(1, LegacyInterfaceListener.RECEIVED.size());
            assertEquals("interface:SKU-7002", LegacyInterfaceListener.RECEIVED.get(0));
            System.out.println("接口式监听器收到包装事件: " + LegacyInterfaceListener.RECEIVED);
        }
    }

    @Test
    public void messageSourceI18n() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CoreEventConfig.class)) {
            // bean 名必须是 messageSource，容器才采用它；basename=message 引用已有资源束（key：app.message）
            assertEquals("Original God", ctx.getMessage("app.message", null, "", Locale.US));
            assertEquals("原神", ctx.getMessage("app.message", null, "", Locale.SIMPLIFIED_CHINESE));
            System.out.println("i18n app.message -> en_US=Original God, zh_CN=原神");
        }
    }

    @Test
    public void listenerOrderAndExceptionInterruptsPropagation() {
        StockEventListeners.EXECUTION.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(OrderingEventConfig.class)) {
            // @Order(10) 先于 @Order(20)
            ctx.publishEvent(new StockMovedEvent("SKU-7003", 5));
            assertEquals(List.of("audit:SKU-7003", "notify:SKU-7003"), StockEventListeners.EXECUTION);
            System.out.println("@Order 监听顺序: " + StockEventListeners.EXECUTION);

            // 同步多播：前面的监听器抛异常 -> 后续监听器不再执行，异常抛回 publishEvent 调用方
            StockEventListeners.EXECUTION.clear();
            assertThrows(IllegalStateException.class,
                    () -> ctx.publishEvent(new AuditAlarmEvent("库存对不上")));
            assertEquals(List.of("alarm-throwing"), StockEventListeners.EXECUTION);
            System.out.println("同步多播异常中断: 实际执行 " + StockEventListeners.EXECUTION);
        }
    }

    @Test
    public void asyncListenerRunsOnCustomExecutor() throws InterruptedException {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncEventConfig.class)) {
            ctx.publishEvent(new ShipmentDispatchedEvent("TRK-7004"));
            // 发布方立即返回，异步监听器在自定义线程池执行完（CountDownLatch 同步，不裸 sleep）
            assertTrue(ShipmentAsyncListener.DONE.await(3, TimeUnit.SECONDS), "异步监听器应在 3 秒内完成");
            System.out.println("异步监听线程: " + ShipmentAsyncListener.workerThreadName
                    + "，发布线程: " + Thread.currentThread().getName());
            assertNotEquals(Thread.currentThread().getName(), ShipmentAsyncListener.workerThreadName);
            assertTrue(ShipmentAsyncListener.workerThreadName.startsWith("shipment-worker-"),
                    "应运行在自定义 executor 线程上");
        }
    }

    @Test
    public void conditionalListenerFiltersBySpel() {
        ThresholdAlertListener.ALERTS.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(OrderingEventConfig.class)) {
            // condition 为 false 的事件被整体跳过，方法体不执行
            ctx.publishEvent(new StockMovedEvent("SKU-7008", 3));
            assertTrue(ThresholdAlertListener.ALERTS.isEmpty(), "小额变动不应触发告警");
            // condition 为 true 才进入监听器
            ctx.publishEvent(new StockMovedEvent("SKU-7009", 77));
            assertEquals(List.of("SKU-7009:77"), ThresholdAlertListener.ALERTS);
            System.out.println("condition(SpEL) 过滤: 小额跳过、大额命中 " + ThresholdAlertListener.ALERTS);
        }
    }

    @Test
    public void builtinContextEvents() {
        ContextLifecycleRecorder.TIMELINE.clear();
        // 构造即 refresh -> refreshed；显式 start() -> started；try-with-resources 关闭 -> closed
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LifecycleEventConfig.class)) {
            assertEquals(List.of("refreshed"), ContextLifecycleRecorder.TIMELINE);
            ctx.start();
            assertEquals(List.of("refreshed", "started"), ContextLifecycleRecorder.TIMELINE);
        }
        assertEquals(List.of("refreshed", "started", "closed"), ContextLifecycleRecorder.TIMELINE);
        System.out.println("容器内置事件时间线: " + ContextLifecycleRecorder.TIMELINE);
    }
}
