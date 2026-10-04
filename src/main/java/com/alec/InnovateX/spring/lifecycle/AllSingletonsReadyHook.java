package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SmartInitializingSingleton：容器在"所有非懒加载单例全部实例化完毕"之后回调
 * afterSingletonsInstantiated，且整个容器生命周期只回调一次。
 *
 * 与 @PostConstruct 的本质区别（本类把两者放在一起对照）：
 * - @PostConstruct：本 Bean 创建完就触发，此时别的单例可能还没创建；
 * - afterSingletonsInstantiated：全体单例就绪后的"收官"时机，
 *   适合做"依赖所有 Bean 都在"的一次性初始化（如全局缓存预热、路由表汇总）。
 * Spring 事件广播器、@EventListener 的注册时机都以此为界。
 */
public class AllSingletonsReadyHook implements SmartInitializingSingleton {

    /** hook 自己的 @PostConstruct 时刻，worker 是否已就绪（依赖方向保证此时是 false） */
    public static volatile boolean WORKER_READY_AT_POST_CONSTRUCT = false;

    /** 全体单例就绪回调时刻，worker 是否已就绪（必然是 true，这正是该回调的语义） */
    public static volatile boolean WORKER_READY_AT_ALL_SINGLETONS = false;

    /** 回调次数：验证"只回调一次"（跨测试需在测试里重置） */
    public static final AtomicInteger AFTER_SINGLETONS_CALLS = new AtomicInteger();

    @PostConstruct
    public void selfReadyOnly() {
        WORKER_READY_AT_POST_CONSTRUCT = PlainWorkerBean.INITIALIZED;
        LifecycleEventLog.EVENTS.add("hook:@PostConstruct(worker已就绪=" + WORKER_READY_AT_POST_CONSTRUCT + ")");
        System.out.println("[AllSingletonsReadyHook] @PostConstruct：仅本 Bean 就绪，worker已就绪="
                + WORKER_READY_AT_POST_CONSTRUCT);
    }

    @Override
    public void afterSingletonsInstantiated() {
        AFTER_SINGLETONS_CALLS.incrementAndGet();
        WORKER_READY_AT_ALL_SINGLETONS = PlainWorkerBean.INITIALIZED;
        LifecycleEventLog.EVENTS.add("hook:afterSingletonsInstantiated(worker已就绪="
                + WORKER_READY_AT_ALL_SINGLETONS + ")");
        System.out.println("[AllSingletonsReadyHook] afterSingletonsInstantiated：全体非懒单例就绪（含 worker）");
    }
}
