package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.atomic.AtomicInteger;

/** 原型 bean：@PreDestroy 在 prototype 作用域下不会被容器调用（容器不管理其完整生命周期）。 */
public class PrototypeResourceBean {

    private static final AtomicInteger SEQ = new AtomicInteger();

    private final int id = SEQ.incrementAndGet();

    public PrototypeResourceBean() {
        LifecycleLog.record("构造:prototype#" + id);
    }

    public int id() {
        return id;
    }

    @PreDestroy
    void onShutdown() {
        LifecycleLog.record("销毁:prototype#" + id);
    }
}
