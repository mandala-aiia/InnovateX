package com.alec.InnovateX.spring.di;

/** 构造器循环依赖 A。 */
public class CtorA {

    private final CtorB b;

    public CtorA(CtorB b) {
        this.b = b;
    }

    public CtorB getB() {
        return b;
    }
}
