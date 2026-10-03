package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

public class AopCircleB {

    /** 注入进来的 A 是"三级缓存早期曝光的代理"而非原始对象 */
    @Autowired
    private AopCircleA a;

    public AopCircleA getA() {
        return a;
    }
}
