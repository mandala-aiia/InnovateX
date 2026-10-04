package com.alec.InnovateX.spring.aop;

/**
 * final 类——AOP 的硬边界教具。
 * CGLIB 靠"继承目标类生成子类"织入增强，final 类不可继承，
 * 于是对它创建代理会在 getProxy() 当场抛出 IllegalArgumentException（Cannot subclass final class）。
 * 对比：JDK 代理不受影响——它只需要目标类"实现接口"，与目标类本身能否被继承无关。
 */
public final class FinalRateTable {

    public String rate() {
        return "0.038";
    }
}
