package com.alec.InnovateX.spring.scope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * prototype（原型作用域）：每次 getBean 都让容器现场 new 一个新实例；
 * 容器只负责"创建 + 初始化 + 依赖注入"，创建完就交还调用方，
 * <b>不再跟踪其生命周期</b>——@PreDestroy 永远不会被容器触发，销毁要自己负责。
 */
public class Ticket {

    /** 演示状态：实例创建计数 */
    public static final AtomicInteger CREATED = new AtomicInteger();

    /** 演示状态：销毁回调计数——断言它保持 0，说明容器不管 prototype 的死活 */
    public static final AtomicInteger DESTROYED = new AtomicInteger();

    private final int number;

    public Ticket(int number) {
        this.number = number;
        CREATED.incrementAndGet();
        System.out.println("[Ticket] prototype 实例化，号码=" + number
                + "（第 " + CREATED.get() + " 个实例）@" + Integer.toHexString(System.identityHashCode(this)));
    }

    @jakarta.annotation.PreDestroy
    public void onClose() {
        DESTROYED.incrementAndGet();
        System.out.println("[Ticket] @PreDestroy 被调用——prototype 场景下这行理论上永远不会打印");
    }

    public int getNumber() {
        return number;
    }

    /** 输出身份信息：用 identityHashCode 区分"是不是同一个对象" */
    public String describe() {
        return "Ticket#" + number + " @" + Integer.toHexString(System.identityHashCode(this));
    }
}
