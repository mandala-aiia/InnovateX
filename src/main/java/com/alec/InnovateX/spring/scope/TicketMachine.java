package com.alec.InnovateX.spring.scope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * singleton（单例作用域，容器默认）：整个容器生命周期只实例化一次，
 * 所有人拿到的都是同一个对象，创建与销毁都由容器统一管理。
 *
 * 对比点：与 {@link Ticket}（prototype）放在同一个配置里，
 * 观察"同引用 vs 新实例"以及"容器是否负责销毁"的差异。
 */
public class TicketMachine {

    /** 演示状态：实例创建计数（AtomicInteger 保证多线程读取可见、累加原子） */
    public static final AtomicInteger CREATED = new AtomicInteger();

    /** 演示状态：@PreDestroy 回调计数——singleton 的销毁由容器负责 */
    public static final AtomicInteger DESTROYED = new AtomicInteger();

    /** 业务状态：当天已发出的号（单例内可变状态被所有调用方共享，这正是 singleton 的语义） */
    private final AtomicInteger issued = new AtomicInteger();

    public TicketMachine() {
        CREATED.incrementAndGet();
        System.out.println("[TicketMachine] singleton 实例化 #" + CREATED.get()
                + " @" + Integer.toHexString(System.identityHashCode(this)));
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        DESTROYED.incrementAndGet();
        System.out.println("[TicketMachine] 容器关闭触发 @PreDestroy（singleton 归容器管）");
    }

    /** 取号：同一天内号码单调递增，印证"单例共享同一状态" */
    public int nextNumber() {
        int no = issued.incrementAndGet();
        System.out.println("[TicketMachine] 发号 " + no + "（累计已发 " + no + " 张）");
        return no;
    }

    public int getIssued() {
        return issued.get();
    }
}
