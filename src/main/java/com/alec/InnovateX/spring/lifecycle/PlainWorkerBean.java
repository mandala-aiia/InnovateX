package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PostConstruct;

/**
 * 最普通的单例 Bean：只有 @PostConstruct。
 * 它是 SmartInitializingSingleton 的"对照物"——
 * @PostConstruct 只代表"我自己就绪"，而 afterSingletonsInstantiated 代表"全体非懒单例都就绪"。
 *
 * 构造器注入 AllSingletonsReadyHook 是刻意的依赖设计：保证 hook 一定先于本 Bean 创建，
 * 于是"hook 自己的 @PostConstruct 时刻 worker 尚未就绪"这件事由依赖方向保证，不靠 @Bean 声明顺序。
 */
public class PlainWorkerBean {

    /** 本 Bean 的 @PostConstruct 是否已执行 */
    public static volatile boolean INITIALIZED = false;

    private final AllSingletonsReadyHook hook;

    /** 显式构造器注入 + final 字段：业务类推荐写法（对比满配 Bean 特意的 setter 注入） */
    public PlainWorkerBean(AllSingletonsReadyHook hook) {
        this.hook = hook;
    }

    public AllSingletonsReadyHook getHook() {
        return hook;
    }

    @PostConstruct
    public void markReady() {
        INITIALIZED = true;
        LifecycleEventLog.EVENTS.add("worker:@PostConstruct");
        System.out.println("[PlainWorkerBean] @PostConstruct：普通单例自己就绪了");
    }
}
