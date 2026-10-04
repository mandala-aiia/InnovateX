package com.alec.InnovateX.spring.scope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * thread 作用域的演示 Bean：用来观察"同线程共享、跨线程隔离"。
 * 创建计数用 AtomicInteger——实例会在多个用户线程上并发创建，volatile int++ 不安全。
 */
public class TraceTag {

    /** 演示状态：跨线程统计总实例数 */
    public static final AtomicInteger CREATED = new AtomicInteger();

    /** 演示状态：销毁计数——destroyScopedBean/remove 路径会触发 @PreDestroy */
    public static final AtomicInteger DESTROYED = new AtomicInteger();

    private final String threadName = Thread.currentThread().getName();

    public TraceTag() {
        CREATED.incrementAndGet();
        System.out.println("[TraceTag] 创建第 " + CREATED.get() + " 个实例，归属线程=" + threadName);
    }

    public String describe() {
        return "TraceTag@" + Integer.toHexString(System.identityHashCode(this)) + "（线程=" + threadName + "）";
    }

    @jakarta.annotation.PreDestroy
    public void onThreadDiscard() {
        DESTROYED.incrementAndGet();
        System.out.println("[TraceTag] 销毁回调执行，实例归属线程=" + threadName
                + "（由容器销毁路径触发，含 scope.remove + destroyBean）");
    }
}
