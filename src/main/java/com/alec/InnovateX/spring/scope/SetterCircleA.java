package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/** setter 循环依赖 A：属性填充阶段可借助三级缓存暴露早期引用，默认能成功 */
public class SetterCircleA {

    @Autowired
    private SetterCircleB b;

    public SetterCircleB getB() {
        return b;
    }
}
