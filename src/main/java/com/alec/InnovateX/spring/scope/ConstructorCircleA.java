package com.alec.InnovateX.spring.scope;

/** 构造器循环依赖 A：构造时就要 B，B 构造时又反过要 A，谁也无法完成实例化 */
public class ConstructorCircleA {

    public ConstructorCircleA(ConstructorCircleB b) {
        System.out.println("[ConstructorCircleA] 构造完成，持有 B");
    }
}
