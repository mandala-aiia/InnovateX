package com.alec.InnovateX.spring.testctx;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 上下文创建计数器：每次 ApplicationContext 新建都会构造一次本类，
 * 静态计数就是"TestContext 上下文缓存 / @DirtiesContext 重建"的观测窗口。
 * 手写 AnnotationConfigApplicationContext 的测试每个方法都各建各的上下文；
 * TestContext 则按"配置键"缓存复用——这是它最大的性能卖点
 */
public class ContextCreationCounter {

    public static final AtomicInteger CONTEXTS_CREATED = new AtomicInteger();

    public ContextCreationCounter() {
        CONTEXTS_CREATED.incrementAndGet();
    }

    public int createdCount() {
        return CONTEXTS_CREATED.get();
    }
}
