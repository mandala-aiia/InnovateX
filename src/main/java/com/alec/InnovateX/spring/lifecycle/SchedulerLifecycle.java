package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.SmartLifecycle;

/** 调度器：phase=100 的 SmartLifecycle，后启动、先停止（依赖低 phase 组件就绪）。 */
public class SchedulerLifecycle implements SmartLifecycle {

    private boolean running;

    @Override
    public void start() {
        running = true;
        LifecycleLog.record("SL.start[phase=100:scheduler]");
    }

    @Override
    public void stop() {
        running = false;
        LifecycleLog.record("SL.stop[phase=100:scheduler]");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 100;
    }
}
