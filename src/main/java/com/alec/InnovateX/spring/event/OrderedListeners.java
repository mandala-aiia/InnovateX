package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 监听器顺序与异常传播：
 * - @Order 值小的先执行（同一事件的多个监听器排序）
 * - 同步多播（SimpleApplicationEventMulticaster 无 executor）时，某个监听器抛异常会中断
 *   本事件后续监听器的执行，并把异常抛回 publishEvent 调用方
 */
@Component
public class OrderedListeners {

    public static final List<String> EXECUTION = new CopyOnWriteArrayList<>();

    @Component
    public static class FirstListener {
        @Order(1)
        @EventListener
        public void onEvent(OrderCreatedEvent event) {
            EXECUTION.add("first");
            System.out.println("[OrderedListeners] 第 1 个监听器 (order=1)");
        }
    }

    @Component
    public static class SecondListener {
        @Order(2)
        @EventListener
        public void onEvent(OrderCreatedEvent event) {
            EXECUTION.add("second");
            System.out.println("[OrderedListeners] 第 2 个监听器 (order=2)");
        }
    }

    /** 抛异常的监听器：验证异常会中断同事件后续监听器并传播给发布方 */
    @Component
    public static class ThrowingListener {
        @Order(1)
        @EventListener
        public void onRiskyEvent(RiskyEvent event) {
            EXECUTION.add("throwing");
            throw new IllegalStateException("监听器主动抛异常");
        }
    }

    @Component
    public static class NeverReachedListener {
        @Order(2)
        @EventListener
        public void onRiskyEvent(RiskyEvent event) {
            EXECUTION.add("never-reached");
        }
    }
}
