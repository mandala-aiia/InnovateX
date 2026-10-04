package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.SmartLifecycle;

/**
 * SmartLifecycle（phase = 10，基础设施位）：模拟"被别人依赖的底层组件"（如消息存储）。
 *
 * SmartLifecycle 对比普通 Lifecycle 的两点增强：
 * 1) getPhase()：phase 小的先启动、后停止——依赖别人的组件给大 phase，
 *    容器由此自动排出"依赖先就绪、后关闭"的启停拓扑（无需手动 context.start()/stop()）；
 * 2) isAutoStartup()：true 时 refresh 收尾自动 start，普通 Lifecycle 必须手动调 context.start()。
 */
public class StorageLifecycle implements SmartLifecycle {

    private volatile boolean running = false;

    @Override
    public void start() {
        running = true;
        LifecycleEventLog.EVENTS.add("storage:start(phase=10)");
        System.out.println("[StorageLifecycle] start()（phase=10，先启动）");
    }

    @Override
    public void stop() {
        running = false;
        LifecycleEventLog.EVENTS.add("storage:stop(phase=10)");
        System.out.println("[StorageLifecycle] stop()（phase=10，后停止）");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 10;
    }

    /** 显式声明（默认即 true）：refresh 完成后由容器自动 start，测试从未手动调用过 start() */
    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
