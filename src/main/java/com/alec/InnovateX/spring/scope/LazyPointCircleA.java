package com.alec.InnovateX.spring.scope;

/** @Lazy 打破构造器循环依赖：A 持有真实的 B，B 持有 A 的"懒代理" */
public class LazyPointCircleA {

    public String hello() {
        return "LazyPointCircleA.hello @" + Integer.toHexString(System.identityHashCode(this));
    }
}
