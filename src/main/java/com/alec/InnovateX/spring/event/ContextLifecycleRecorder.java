package com.alec.InnovateX.spring.event;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 容器内置事件（ApplicationContext 生命周期自身发布的 ApplicationEvent 子类）：
 * - ContextRefreshedEvent：refresh() 完成时发布（new AnnotationConfigApplicationContext(Config.class) 构造即触发）
 * - ContextStartedEvent：显式调用 context.start() 时发布（Lifecycle 手动启动信号）
 * - ContextClosedEvent：context.close() 时发布（try-with-resources 关容器就会看到）
 *
 * 辨析：SmartLifecycle(autoStartup=true) 的 Bean 在 refresh 末尾就自动 start 了，
 * 而 ContextStartedEvent 只跟随"手动 start()"——两者不是一回事。
 */
public class ContextLifecycleRecorder {

    public static final List<String> TIMELINE = new CopyOnWriteArrayList<>();

    @EventListener
    public void onRefreshed(ContextRefreshedEvent event) {
        TIMELINE.add("refreshed");
        System.out.println("[ContextLifecycleRecorder] ContextRefreshedEvent：容器刷新完成");
    }

    @EventListener
    public void onStarted(ContextStartedEvent event) {
        TIMELINE.add("started");
        System.out.println("[ContextLifecycleRecorder] ContextStartedEvent：手动 start() 被调用");
    }

    @EventListener
    public void onClosed(ContextClosedEvent event) {
        TIMELINE.add("closed");
        System.out.println("[ContextLifecycleRecorder] ContextClosedEvent：容器已关闭");
    }
}
