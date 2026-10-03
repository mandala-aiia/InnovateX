package com.alec.InnovateX.spring.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

/**
 * 异步与调度配置：
 * - 实现 AsyncConfigurer：@Async 用 getAsyncExecutor 的线程池，
 *   void 异步方法的异常由 getAsyncUncaughtExceptionHandler 兜底
 * - 实现 SchedulingConfigurer：@Scheduled 用指定的调度线程池（默认单线程 scheduler）
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan
public class AsyncSchedulingConfig implements AsyncConfigurer, SchedulingConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("demo-async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            AsyncJobService.uncaughtMessage = method.getName() + " -> " + ex.getMessage();
            AsyncJobService.UNCAUGHT_LATCH.countDown();
            System.out.println("[AsyncUncaughtExceptionHandler] 捕获异步异常: " + AsyncJobService.uncaughtMessage);
        };
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("demo-sched-");
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
