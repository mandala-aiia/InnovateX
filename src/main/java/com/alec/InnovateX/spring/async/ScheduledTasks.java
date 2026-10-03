package com.alec.InnovateX.spring.async;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Scheduled 定时任务：
 * - fixedDelay：上次执行"结束"到下次"开始"的间隔
 * - cron：cron 表达式（6 位：秒 分 时 日 月 周）
 * 计数器 + CountDownLatch 供测试验证确实被周期性调度
 */
@Component
public class ScheduledTasks {

    public static final AtomicInteger FIXED_COUNT = new AtomicInteger();

    public static final CountDownLatch FIXED_LATCH = new CountDownLatch(3);

    @Scheduled(fixedDelay = 50, initialDelay = 50)
    public void fixedDelayTask() {
        FIXED_COUNT.incrementAndGet();
        FIXED_LATCH.countDown();
        System.out.println("[ScheduledTasks] fixedDelay 第 " + FIXED_COUNT.get() + " 次执行，线程="
                + Thread.currentThread().getName());
    }

    public static final java.util.concurrent.atomic.AtomicInteger CRON_COUNT = new java.util.concurrent.atomic.AtomicInteger();

    public static final java.util.concurrent.CountDownLatch CRON_LATCH = new java.util.concurrent.CountDownLatch(2);

    /** cron 表达式 6 位：秒 分 时 日 月 周——这里"每秒的第 0 秒"即每秒执行一次 */
    @Scheduled(cron = "*/1 * * * * *")
    public void cronTask() {
        CRON_COUNT.incrementAndGet();
        CRON_LATCH.countDown();
        System.out.println("[ScheduledTasks] cron 第 " + CRON_COUNT.get() + " 次执行，线程="
                + Thread.currentThread().getName());
    }
}
