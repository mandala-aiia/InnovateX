package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.SmartLifecycle;

/** 指标上报器：phase=0 的 SmartLifecycle，先启动、后停止。 */
public class MetricsLifecycle implements SmartLifecycle {

    private boolean running;

    @Override
    public void start() {
        running = true;
        LifecycleLog.record("SL.start[phase=0:metrics]");
    }

    @Override
    public void stop() {
        running = false;
        LifecycleLog.record("SL.stop[phase=0:metrics]");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
