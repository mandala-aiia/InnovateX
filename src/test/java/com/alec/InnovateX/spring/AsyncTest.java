package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.async.AsyncSchedulingConfig;
import com.alec.InnovateX.spring.async.AsyncWorkService;
import com.alec.InnovateX.spring.async.HeartbeatTasks;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题：异步与调度。覆盖知识点：
 * 1. @EnableAsync + @Async 返回 CompletableFuture（调用立即返回、结果异步就绪）；
 * 2. AsyncConfigurer 自定义线程池（teach-async- 前缀 + 非调用者线程断言）；
 * 3. void @Async 方法异常由 AsyncUncaughtExceptionHandler 兜底（latch 等待，不裸 sleep）；
 * 4. @EnableScheduling + @Scheduled fixedDelay 与 cron 被周期调度（latch+超时计数断言）；
 * 5. SchedulingConfigurer 自定义调度线程池（teach-sched- 前缀断言）。
 */
public class AsyncTest {

    @Test
    public void asyncCompletableFutureRunsOnCustomPool() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncSchedulingConfig.class)) {
            AsyncWorkService service = ctx.getBean(AsyncWorkService.class);
            int threadsBefore = AsyncWorkService.WORKER_THREADS.size();

            CompletableFuture<String> future = service.renderReport("Q3 业绩");
            System.out.println("[AsyncTest] @Async 调用立即返回，主线程继续（未被阻塞）");

            // Future 语义取结果（带超时，绝不裸 sleep）
            assertEquals("report:Q3 业绩", future.get(3, TimeUnit.SECONDS));

            // 自定义线程池断言：线程名前缀 teach-async-，且不是调用者线程
            assertEquals(threadsBefore + 1, AsyncWorkService.WORKER_THREADS.size());
            String worker = AsyncWorkService.WORKER_THREADS.get(AsyncWorkService.WORKER_THREADS.size() - 1);
            assertTrue(worker.startsWith("teach-async-"), "应运行在 AsyncConfigurer 自定义线程池，实际=" + worker);
            assertNotEquals(Thread.currentThread().getName(), worker, "不能在调用者线程执行");
            System.out.println("[AsyncTest] 异步线程=" + worker + "，主线程=" + Thread.currentThread().getName());
        }
    }

    @Test
    public void voidAsyncExceptionHandledByFallbackHandler() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncSchedulingConfig.class)) {
            AsyncWorkService service = ctx.getBean(AsyncWorkService.class);
            // 调用立即返回，异常不会传播给调用方
            service.fireAndForgetFails();

            // AsyncUncaughtExceptionHandler 兜底：用 latch 等待回调发生
            assertTrue(AsyncWorkService.UNCAUGHT_LATCH.await(3, TimeUnit.SECONDS),
                    "3 秒内应收到异步异常兜底回调");
            assertNotNull(AsyncWorkService.UNCAUGHT_DETAIL);
            assertTrue(AsyncWorkService.UNCAUGHT_DETAIL.contains("fireAndForgetFails"), "应包含方法名");
            assertTrue(AsyncWorkService.UNCAUGHT_DETAIL.contains("异步void方法的故意异常"), "应包含异常消息");
            System.out.println("[AsyncTest] 兜底结果: " + AsyncWorkService.UNCAUGHT_DETAIL);
        }
    }

    @Test
    public void scheduledTasksFireOnCustomSchedulerPool() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncSchedulingConfig.class)) {
            // fixedDelay：容器开启期间被周期性调度（latch + 超时，不裸 sleep）
            assertTrue(HeartbeatTasks.FIXED_DELAY_LATCH.await(5, TimeUnit.SECONDS),
                    "fixedDelay 任务应至少执行 3 次，实际 " + HeartbeatTasks.FIXED_DELAY_RUNS.get());
            assertTrue(HeartbeatTasks.FIXED_DELAY_RUNS.get() >= 3);

            // cron（*/1 * * * * * 每秒一次）
            assertTrue(HeartbeatTasks.CRON_LATCH.await(6, TimeUnit.SECONDS),
                    "cron 任务应至少执行 2 次，实际 " + HeartbeatTasks.CRON_RUNS.get());
            assertTrue(HeartbeatTasks.CRON_RUNS.get() >= 2);
            System.out.println("[AsyncTest] fixedDelay 已执行 " + HeartbeatTasks.FIXED_DELAY_RUNS.get()
                    + " 次，cron 已执行 " + HeartbeatTasks.CRON_RUNS.get() + " 次");

            // SchedulingConfigurer 自定义调度线程池：所有调度都在 teach-sched- 前缀线程上
            assertTrue(HeartbeatTasks.SCHEDULER_THREADS.size() >= 5, "应已积累足够的调度记录");
            for (String thread : HeartbeatTasks.SCHEDULER_THREADS) {
                assertTrue(thread.startsWith("teach-sched-"),
                        "调度应发生在自定义调度线程池，实际=" + thread);
            }
            System.out.println("[AsyncTest] 调度线程样本: " + HeartbeatTasks.SCHEDULER_THREADS);
        }
    }
}
