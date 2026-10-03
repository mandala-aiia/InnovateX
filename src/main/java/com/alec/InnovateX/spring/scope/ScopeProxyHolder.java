package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 单例 Bean 持有 prototype Bean 的"作用域代理"：
 * 注入进来的不是 PrototypeTargetBean 本尊，而是一个 CGLIB 代理对象；
 * 每次调用代理的方法时，它才去容器 getBean 取一个全新的 prototype 实例来执行
 */
public class ScopeProxyHolder {

    @Autowired
    private PrototypeTargetBean prototypeTargetBean;

    public String firstCall() {
        return prototypeTargetBean.whoAmI();
    }

    public String secondCall() {
        return prototypeTargetBean.whoAmI();
    }
}
