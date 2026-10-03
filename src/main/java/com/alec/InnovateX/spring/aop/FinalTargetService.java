package com.alec.InnovateX.spring.aop;

/**
 * final 类：CGLIB 通过"生成子类"实现代理，final 类无法被继承，
 * 对它强制 CGLIB 代理会在创建代理时直接抛异常（JDK 代理则需要目标类实现接口）
 */
public final class FinalTargetService {

    public String ping() {
        return "pong";
    }
}
