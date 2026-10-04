package com.alec.InnovateX.spring.spel;

/** 简单计数器 POJO：给 #{@seedGenerator.base} 这类"Bean 引用表达式"当靶子 */
public class SeedGenerator {

    private final int base;

    public SeedGenerator(int base) {
        this.base = base;
        System.out.println("[SeedGenerator] 创建，base=" + base);
    }

    public int getBase() {
        return base;
    }
}
