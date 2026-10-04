package com.alec.InnovateX.spring.scope;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.atomic.AtomicInteger;

/** 原型产品：每次获取都新造一个；@PreDestroy 是否被调用取决于作用域是否由容器托管销毁。 */
public class PrototypeProduct {

    private static final AtomicInteger SEQ = new AtomicInteger();

    private final int id = SEQ.incrementAndGet();

    public PrototypeProduct() {
        ScopeLog.record("创建:PrototypeProduct#" + id);
    }

    public int id() {
        return id;
    }

    @PreDestroy
    void shutdown() {
        ScopeLog.record("销毁:PrototypeProduct#" + id);
    }
}
