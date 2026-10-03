package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/** AOP 循环依赖 A：hello() 被切面命中，正常流程下 A 初始化完成后才生成代理 */
public class AopCircleA {

    @Autowired
    private AopCircleB b;

    public String hello() {
        return "AopCircleA.hello @" + Integer.toHexString(System.identityHashCode(this));
    }

    public AopCircleB getB() {
        return b;
    }
}
