package com.alec.InnovateX.spring.scope;

public class LazyPointCircleB {

    private final Object lazyA;

    public LazyPointCircleB(Object lazyA) {
        this.lazyA = lazyA;
    }

    /** 调用时才触发代理去容器解析真实的 A */
    public String callA() {
        try {
            return (String) lazyA.getClass().getMethod("hello").invoke(lazyA);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Object getLazyA() {
        return lazyA;
    }
}
