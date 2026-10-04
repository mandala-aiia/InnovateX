package com.alec.InnovateX.spring.scope;

/** 自定义作用域目标：配合 SimpleThreadScope，同一线程内共享、跨线程隔离。 */
public class ThreadLocalResource {

    private static final java.util.concurrent.atomic.AtomicInteger SEQ =
            new java.util.concurrent.atomic.AtomicInteger();

    public final int id = SEQ.incrementAndGet();

    public ThreadLocalResource() {
        ScopeLog.record("创建:ThreadLocalResource#" + id + "@线程" + Thread.currentThread().threadId());
    }

    public int id() {
        return id;
    }
}
