package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PreDestroy;

/** 被 @DependsOn 依赖的 bean：应先于 beta 初始化、后于 beta 销毁。 */
public class AlphaBean {

    public AlphaBean() {
        LifecycleLog.record("构造:alpha");
    }

    @PreDestroy
    void onShutdown() {
        LifecycleLog.record("销毁:alpha");
    }
}
