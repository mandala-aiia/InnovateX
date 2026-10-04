package com.alec.InnovateX.spring.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步监听演示装配：
 *
 * - @EnableAsync 注册 AsyncAnnotationBeanPostProcessor，给带 @Async 方法的 Bean 织入"提交到线程池"的代理。
 * - executor 查找规则（AsyncExecutionAspectSupport.getDefaultExecutor）：
 *   容器中"唯一的 TaskExecutor 类型 Bean"优先被采用；否则找名为 taskExecutor 的 Executor Bean；
 *   都没有就兜底 SimpleAsyncTaskExecutor（不复用线程，生产慎用）。
 *   这里提供唯一的 ThreadPoolTaskExecutor，线程名前缀 shipment-worker- 供测试断言。
 * - ThreadPoolTaskExecutor 实现 InitializingBean：作为 @Bean 由容器自动调用 afterPropertiesSet()
 *   完成初始化，不必（也不应）在 @Bean 方法里手动 initialize()。
 */
@Configuration
@EnableAsync
public class AsyncEventConfig {

    @Bean
    public ThreadPoolTaskExecutor shipmentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("shipment-worker-");
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(16);
        return executor;
    }

    @Bean
    public ShipmentAsyncListener shipmentAsyncListener() {
        return new ShipmentAsyncListener();
    }
}
