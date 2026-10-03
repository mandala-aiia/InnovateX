package com.alec.InnovateX.spring.scope;

public class ConstructorCircleB {

    public ConstructorCircleB(ConstructorCircleA a) {
        System.out.println("[ConstructorCircleB] 构造完成，持有 A");
    }
}
