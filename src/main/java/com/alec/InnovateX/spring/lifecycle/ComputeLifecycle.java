package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.SmartLifecycle;

/**
 * SmartLifecycle（phase = 20，依赖者位）：模拟"依赖底层组件的上层组件"（如计算引擎依赖存储）。
 *
 * phase 排序的含义在本类可被直接验证：
 * - start() 被调用时（phase 升序，20 晚于 10），依赖的 StorageLifecycle 必然已经 running；
 * - stop() 被调用时（phase 降序，20 先于 10），StorageLifecycle 必然还在 running——
 *   "先启动的最后停"，依赖方永远在被依赖方存活期间优雅收尾。
 *
 * 本类同时是"显式构造器注入 + final 字段 + 显式 getter"的业务类规范写法示范。
 */
public class ComputeLifecycle implements SmartLifecycle {

    private final StorageLifecycle storage;

    private volatile boolean running = false;

    public ComputeLifecycle(StorageLifecycle storage) {
        this.storage = storage;
    }

    public StorageLifecycle getStorage() {
        return storage;
    }

    @Override
    public void start() {
        running = true;
        LifecycleEventLog.EVENTS.add("compute:start(phase=20,storage运行中=" + storage.isRunning() + ")");
        System.out.println("[ComputeLifecycle] start()（phase=20，此时依赖的存储已运行=" + storage.isRunning() + "）");
    }

    @Override
    public void stop() {
        running = false;
        LifecycleEventLog.EVENTS.add("compute:stop(storage仍在运行=" + storage.isRunning() + ")");
        System.out.println("[ComputeLifecycle] stop()（phase=20 先停，此时依赖的存储仍在运行=" + storage.isRunning() + "）");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 20;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
