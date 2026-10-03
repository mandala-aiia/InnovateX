package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/** SmartLifecycle（phase = 2）：phase 更大，后启动、先停止（依赖 phase 小的组件的场景） */
@Component
public class PhaseTwoLifecycle implements SmartLifecycle {

    private volatile boolean running = false;

    @Override
    public void start() {
        running = true;
        PhaseOneLifecycle.EVENTS.add("phase2-start");
        System.out.println("[PhaseTwoLifecycle] start()（phase=2，后启动）");
    }

    @Override
    public void stop() {
        running = false;
        PhaseOneLifecycle.EVENTS.add("phase2-stop");
        System.out.println("[PhaseTwoLifecycle] stop()（phase=2，先停止）");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 2;
    }
}
