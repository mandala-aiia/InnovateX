package com.alec.InnovateX.spring.scope;

/** 被 scoped-proxy 代理的 prototype 目标 Bean：每次方法调用都会通过代理拿到新实例 */
public class PrototypeTargetBean {

    public String whoAmI() {
        return "PrototypeTargetBean @" + Integer.toHexString(System.identityHashCode(this));
    }
}
