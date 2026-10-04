package com.alec.InnovateX.spring.lifecycle;

/** 老式 bean：initMethod/destroyMethod 由 @Bean 属性指定，兼容无法改源码的历史类。 */
public class LegacyCalendarBean {

    private String owner = "未设置";

    public LegacyCalendarBean() {
        LifecycleLog.record("构造:lifeLegacy");
    }

    public void setOwner(String owner) {
        this.owner = owner;
        LifecycleLog.record("属性注入:owner=" + owner);
    }

    public void startWork() {
        LifecycleLog.record("initMethod:startWork(owner=" + owner + ")");
    }

    public void offWork() {
        LifecycleLog.record("destroyMethod:offWork");
    }
}
