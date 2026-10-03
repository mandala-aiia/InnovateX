package com.alec.InnovateX.spring.scope;

/** thread 作用域的 Bean：同线程共享、跨线程隔离 */
public class ThreadScopedBean {

    public static volatile int instanceCount = 0;

    public ThreadScopedBean() {
        instanceCount++;
    }

    public String whoAmI() {
        return "ThreadScopedBean @" + Integer.toHexString(System.identityHashCode(this))
                + "，线程=" + Thread.currentThread().getName();
    }
}
