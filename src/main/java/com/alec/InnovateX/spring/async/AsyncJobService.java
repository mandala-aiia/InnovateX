package com.alec.InnovateX.spring.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @Async 方法演示：
 * - 返回 CompletableFuture：调用方拿到 Future 异步取结果
 * - void 方法抛异常：由 AsyncUncaughtExceptionHandler 兜底（不会传播给调用方）
 * 执行线程是 @EnableAsync 指定的线程池（对比调用者线程）
 */
@Component
public class AsyncJobService {

    public static final CountDownLatch UNCAUGHT_LATCH = new CountDownLatch(1);

    public static volatile String uncaughtMessage;

    public static volatile String lastAsyncThread;

    @Async
    public CompletableFuture<String> slowJob(String input) throws InterruptedException {
        lastAsyncThread = Thread.currentThread().getName();
        Thread.sleep(100);
        return CompletableFuture.completedFuture("processed: " + input);
    }

    @Async
    public void voidJobWithException() {
        lastAsyncThread = Thread.currentThread().getName();
        throw new IllegalStateException("异步 void 方法的异常");
    }
}
