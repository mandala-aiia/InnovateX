package com.alec.InnovateX.spring.scope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Lazy 打破构造器循环依赖的"被懒加载端"：
 * 配置里同时给 @Bean 加 @Lazy（不提前实例化）+ 注入点加 @Lazy（注入代理），
 * 这样它的构造器只在"第一次被真正调用"时才执行——测试据此断言：
 * refresh 完成时 CREATED 仍为 0，首次调用后才变成 1。
 */
public class Console {

    /** 演示状态：观察"首次调用才解析目标"的时刻 */
    public static final AtomicInteger CREATED = new AtomicInteger();

    private final Engine engine;

    public Console(Engine engine) {
        this.engine = engine;
        CREATED.incrementAndGet();
        System.out.println("[Console] 真实目标被构造（第 " + CREATED.get() + " 次）@"
                + Integer.toHexString(System.identityHashCode(this)));
    }

    public String render() {
        return "Console.render@" + Integer.toHexString(System.identityHashCode(this))
                + "，引擎=" + engine.signature();
    }
}
