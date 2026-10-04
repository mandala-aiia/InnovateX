package com.alec.InnovateX.spring.scope;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 三级缓存 + AOP 的"被代理端"：它的 ring() 会被自制代理后置处理器拦截，
 * 同时它与 MonitorStation 互相注入形成 setter 循环——两条知识线在一个场景里交汇。
 */
public class AlarmCenter {

    /** 演示状态：真实目标实例计数 */
    public static final AtomicInteger CREATED = new AtomicInteger();

    @Autowired
    private MonitorStation monitorStation;

    public AlarmCenter() {
        CREATED.incrementAndGet();
        System.out.println("[AlarmCenter] 目标实例构造 @" + Integer.toHexString(System.identityHashCode(this)));
    }

    public MonitorStation getMonitorStation() {
        return monitorStation;
    }

    /** 该方法将被代理拦截：测试调用它时观察拦截日志 */
    public String ring() {
        return "AlarmCenter.ring@" + Integer.toHexString(System.identityHashCode(this));
    }
}
