package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PreDestroy;

/** @DependsOn("alphaBean") 的 bean：即使声明顺序在前，也要等 alpha 就绪才初始化。 */
public class BetaBean {

    public BetaBean() {
        LifecycleLog.record("构造:beta");
    }

    @PreDestroy
    void onShutdown() {
        LifecycleLog.record("销毁:beta");
    }
}
