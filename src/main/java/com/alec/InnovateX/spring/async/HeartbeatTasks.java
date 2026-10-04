package com.alec.InnovateX.spring.async;

import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Scheduled 定时任务演示：
 * - fixedDelay = 100：上一次执行【结束】到下一次【开始】的间隔（对比 fixedRate 是固定频率、
 *   不等上一次结束；initialDelay 是首次延迟）；
 * - cron：六位表达式（秒 分 时 日 月 周），本例设为每秒触发一次
 * （表达式写法见方法注解，秒位为"每秒"通配步进）。
 * 执行次数与线程名全部落静态计数器/清单：测试用 CountDownLatch + 超时等待"确实被调度过"，
 * 并用线程名前缀验证任务跑在 SchedulingConfigurer 自定义的调度线程池上。
 */
public class HeartbeatTasks {

    public static final AtomicInteger FIXED_DELAY_RUNS = new AtomicInteger();

    public static final CountDownLatch FIXED_DELAY_LATCH = new CountDownLatch(3);

    public static final AtomicInteger CRON_RUNS = new AtomicInteger();

    public static final CountDownLatch CRON_LATCH = new CountDownLatch(2);

    /** 记录调度执行线程名（应全部是 teach-sched- 前缀 = 自定义调度线程池） */
    public static final List<String> SCHEDULER_THREADS = new CopyOnWriteArrayList<>();

    @Scheduled(fixedDelay = 100, initialDelay = 50)
    public void heartbeat() {
        FIXED_DELAY_RUNS.incrementAndGet();
        SCHEDULER_THREADS.add(Thread.currentThread().getName());
        FIXED_DELAY_LATCH.countDown();
        System.out.println("[HeartbeatTasks] fixedDelay 第 " + FIXED_DELAY_RUNS.get()
                + " 次，线程=" + Thread.currentThread().getName());
    }

    /** cron 六位：秒 分 时 日 月 周——每秒一次 */
    @Scheduled(cron = "*/1 * * * * *")
    public void everySecondTick() {
        CRON_RUNS.incrementAndGet();
        SCHEDULER_THREADS.add(Thread.currentThread().getName());
        CRON_LATCH.countDown();
        System.out.println("[HeartbeatTasks] cron 第 " + CRON_RUNS.get()
                + " 次，线程=" + Thread.currentThread().getName());
    }
}
