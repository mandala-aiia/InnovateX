package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 容器内置事件（ApplicationContext 自己发布的生命周期事件）：
 * - ContextRefreshedEvent：refresh 完成时（构造 AnnotationConfigApplicationContext 即触发）
 * - ContextStartedEvent：显式调用 context.start() 时（Lifecycle 启动）
 * - ContextClosedEvent：context.close() 时
 * 注意 SmartLifecycle 的 autoStartup Bean 在 refresh 结束时就 start 了，
 * 而 ContextStartedEvent 只有手动 start() 才发布——两者不是一回事
 */
@Component
public class BuiltinEventListener {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @EventListener
    public void onRefreshed(ContextRefreshedEvent event) {
        EVENTS.add("refreshed");
        System.out.println("[BuiltinEventListener] ContextRefreshedEvent：容器刷新完成");
    }

    @EventListener
    public void onStarted(ContextStartedEvent event) {
        EVENTS.add("started");
        System.out.println("[BuiltinEventListener] ContextStartedEvent：显式 start() 被调用");
    }

    @EventListener
    public void onClosed(ContextClosedEvent event) {
        EVENTS.add("closed");
        System.out.println("[BuiltinEventListener] ContextClosedEvent：容器关闭");
    }
}
