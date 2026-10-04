package com.alec.InnovateX.spring.async;

import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @Async 异步方法演示：
 * - 返回 CompletableFuture：调用立即返回，任务丢给 @EnableAsync 指定的线程池，
 *   调用方按 Future 语义取结果（对比同步调用阻塞主线程）；
 * - void 方法抛异常：异常无法"返回"给调用方（void 没有承载物），
 *   只能由 AsyncConfigurer 配置的 AsyncUncaughtExceptionHandler 兜底，否则静默吞掉。
 * 两个方法都记录执行线程名，供测试断言"跑在自定义线程池（teach-async- 前缀）而非调用者线程"。
 */
public class AsyncWorkService {

    /** 每次 @Async 执行所在线程名（前缀应满足自定义线程池配置） */
    public static final List<String> WORKER_THREADS = new CopyOnWriteArrayList<>();

    /** 异常兜底回调已触发的信号（测试等待它而不是裸 sleep） */
    public static final CountDownLatch UNCAUGHT_LATCH = new CountDownLatch(1);

    /** 兜底处理器记录的摘要：方法名 + 异常消息 */
    public static volatile String UNCAUGHT_DETAIL;

    @Async
    public CompletableFuture<String> renderReport(String topic) throws InterruptedException {
        WORKER_THREADS.add(Thread.currentThread().getName());
        System.out.println("[AsyncWorkService] 异步线程 " + Thread.currentThread().getName() + " 开始渲染: " + topic);
        Thread.sleep(80); // 模拟耗时任务（方法内部模拟耗时允许；测试侧绝不裸 sleep 等待）
        return CompletableFuture.completedFuture("report:" + topic);
    }

    @Async
    public void fireAndForgetFails() {
        WORKER_THREADS.add(Thread.currentThread().getName());
        throw new IllegalStateException("异步void方法的故意异常");
    }
}
