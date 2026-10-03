package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.async.AsyncJobService;
import com.alec.InnovateX.spring.async.AsyncSchedulingConfig;
import com.alec.InnovateX.spring.async.ScheduledTasks;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑬异步与调度：@Async（自定义线程池/CompletableFuture/未捕获异常兜底）+ @Scheduled
 */
public class AsyncTest {

    @Test
    public void asyncAndScheduled() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncSchedulingConfig.class)) {
            // @Async 返回 Future：调用立即返回，结果异步就绪
            AsyncJobService service = ctx.getBean(AsyncJobService.class);
            var future = service.slowJob("hello");
            System.out.println("@Async 调用立即返回，主线程继续");
            assertEquals("processed: hello", future.get(3, TimeUnit.SECONDS));
            assertNotEquals(Thread.currentThread().getName(), AsyncJobService.lastAsyncThread);
            System.out.println("异步线程: " + AsyncJobService.lastAsyncThread + "，主线程: " + Thread.currentThread().getName());

            // @Async void 方法抛异常：不传播给调用方，由 AsyncUncaughtExceptionHandler 兜底
            service.voidJobWithException();
            assertTrue(AsyncJobService.UNCAUGHT_LATCH.await(3, TimeUnit.SECONDS));
            System.out.println("异常兜底结果: " + AsyncJobService.uncaughtMessage);
            assertTrue(AsyncJobService.uncaughtMessage.contains("voidJobWithException"));
            assertTrue(AsyncJobService.uncaughtMessage.contains("异步 void 方法的异常"));

            // @Scheduled fixedDelay：容器开启期间被周期性调度（等待 3 次）
            assertTrue(ScheduledTasks.FIXED_LATCH.await(5, TimeUnit.SECONDS),
                    "fixedDelay 任务应至少执行 3 次，实际 " + ScheduledTasks.FIXED_COUNT.get());
            assertTrue(ScheduledTasks.FIXED_COUNT.get() >= 3);
            System.out.println("fixedDelay 任务已执行 " + ScheduledTasks.FIXED_COUNT.get() + " 次");

            // @Scheduled cron（*/1 * * * * * 每秒一次）：等待 2 次触发
            assertTrue(ScheduledTasks.CRON_LATCH.await(6, TimeUnit.SECONDS),
                    "cron 任务应至少执行 2 次，实际 " + ScheduledTasks.CRON_COUNT.get());
            assertTrue(ScheduledTasks.CRON_COUNT.get() >= 2);
            System.out.println("cron 任务已执行 " + ScheduledTasks.CRON_COUNT.get() + " 次");
        }
    }
}
