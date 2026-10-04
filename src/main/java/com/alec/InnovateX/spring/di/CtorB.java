package com.alec.InnovateX.spring.di;

/** 构造器循环依赖 B。 */
public class CtorB {

    private final CtorA a;

    public CtorB(CtorA a) {
        this.a = a;
    }

    public CtorA getA() {
        return a;
    }
}
