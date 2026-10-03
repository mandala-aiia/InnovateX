package com.alec.InnovateX.spring.scope;

/** prototype 循环依赖 A：即使 setter 注入也无法解决，因为 prototype 不进三级缓存 */
public class PrototypeCircleA {

    private PrototypeCircleB b;

    public void setB(PrototypeCircleB b) {
        this.b = b;
    }
}
