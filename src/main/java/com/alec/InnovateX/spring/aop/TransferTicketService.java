package com.alec.InnovateX.spring.aop;

import org.springframework.aop.framework.AopContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 自调用失效与修复的对照实验（Spring AOP 是"代理"不是"织入"，这是代理模型的先天盲区）：
 *
 * bookByThis()  —— this.record(...)：this 是目标对象本身而不是代理，
 *                  调用不经过拦截器链，BookingAspect 完全无感（增强失效）。
 * bookByProxy() —— AopContext.currentProxy() 从 ThreadLocal 取回"当前正在执行这次调用的代理"，
 *                  再通过代理调 record()，走完整拦截器链（增强生效）。
 *
 * 前提：@EnableAspectJAutoProxy(exposeProxy = true)（见 AopShowcaseConfig），
 * 框架才会在每次代理调用进入时把代理塞进 AopContext 的 ThreadLocal。
 * 生产中更推荐的解法是"把自调用拆到另一个 Bean"（如 TradeDeskService.settle 的跨 Bean 调用）。
 */
public class TransferTicketService {

    public static final List<String> BOOKINGS = new CopyOnWriteArrayList<>();

    public String bookByThis() {
        return this.record("this");
    }

    public String bookByProxy() {
        return ((TransferTicketService) AopContext.currentProxy()).record("proxy");
    }

    public String record(String via) {
        return "recorded-via-" + via;
    }
}
