package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SmartLifecycle（phase = 1）：
 * - autoStartup=true：容器 refresh 完成后自动 start，无需手动 context.start()
 * - phase 决定启停顺序：启动按 phase 升序，停止按 phase 降序（先启动的最后停止）
 * - 对比已有 spring 包的 LifecycleProcessor：Lifecycle Bean 才是被处理器驱动的对象
 */
@Component
public class PhaseOneLifecycle implements SmartLifecycle {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    private volatile boolean running = false;

    @Override
    public void start() {
        running = true;
        EVENTS.add("phase1-start");
        System.out.println("[PhaseOneLifecycle] start()（phase=1，先启动）");
    }

    @Override
    public void stop() {
        running = false;
        EVENTS.add("phase1-stop");
        System.out.println("[PhaseOneLifecycle] stop()（phase=1，后停止）");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 1;
    }
}
