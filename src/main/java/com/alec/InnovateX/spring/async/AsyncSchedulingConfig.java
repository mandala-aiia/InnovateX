package com.alec.InnovateX.spring.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
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
 * 异步与调度的统一配置：两个 "Configurer" 接口都是"注解开关之外的精细化定制口"。
 *
 * - @EnableAsync 只负责"开关"，线程池不指定时会回退到默认执行器；
 *   实现 AsyncConfigurer.getAsyncExecutor() 即可指定自定义线程池（本例前缀 teach-async-，
 *   测试靠线程名前缀断言它确实接管了 @Async）；
 * - void @Async 方法的异常没有返回值可挂靠，AsyncUncaughtExceptionHandler 是唯一的观察口；
 * - @EnableScheduling 默认用自动创建的单线程调度器（名字不可控）；
 *   实现 SchedulingConfigurer.configureTasks 并 setTaskScheduler 指定自定义调度线程池
 *   （本例前缀 teach-sched-），同样靠线程名前缀验证。
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig implements AsyncConfigurer, SchedulingConfigurer {

    @Bean
    public AsyncWorkService asyncWorkService() {
        return new AsyncWorkService();
    }

    @Bean
    public HeartbeatTasks heartbeatTasks() {
        return new HeartbeatTasks();
    }

    /** @Async 专用线程池：线程名前缀是"哪个池在执行"的直观物证 */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("teach-async-");
        executor.initialize();
        return executor;
    }

    /** void @Async 方法异常的兜底：记录方法名与异常消息，并 countDown 通知测试 */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            AsyncWorkService.UNCAUGHT_DETAIL = method.getName() + " 在异步线程抛出: " + ex.getMessage();
            AsyncWorkService.UNCAUGHT_LATCH.countDown();
            System.out.println("[AsyncUncaughtExceptionHandler] 捕获: " + AsyncWorkService.UNCAUGHT_DETAIL);
        };
    }

    /** @Scheduled 专用调度线程池：不让定时任务挤占 @Async 池，也不依赖默认匿名调度器 */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("teach-sched-");
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
