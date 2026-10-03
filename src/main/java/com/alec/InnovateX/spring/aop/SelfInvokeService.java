package com.alec.InnovateX.spring.aop;

import org.springframework.aop.framework.AopContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 自调用失效与修复：
 * - outerFail() 里 this.inner(...)：this 是目标对象而不是代理，切面对 inner 的增强完全失效
 * - outerFixed() 里拿 AopContext.currentProxy() 再调 inner(...)：走代理，增强生效
 * 前提：@EnableAspectJAutoProxy(exposeProxy = true) 把代理放进 ThreadLocal
 */
public class SelfInvokeService {

    public static final List<String> LOG = new CopyOnWriteArrayList<>();

    public String outerFail() {
        return this.inner("outerFail");
    }

    public String outerFixed() {
        SelfInvokeService proxy = (SelfInvokeService) AopContext.currentProxy();
        return proxy.inner("outerFixed");
    }

    public String inner(String caller) {
        return "inner called by " + caller;
    }
}
