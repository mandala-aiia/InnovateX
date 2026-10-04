package com.alec.InnovateX.spring.event;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 事件演示配置：@ComponentScan 扫描本包 + @EnableAsync 开启 @Async 支持。
 * 注意 @EnableAsync 的 executor 查找规则：容器中"唯一的 TaskExecutor 类型 bean"优先，
 * 否则找名为 taskExecutor 的 bean——提供 ThreadPoolTaskExecutor 类型的 bean 即可被自动采用
 */
@Configuration
@EnableAsync
@ComponentScan
public class EventConfig {

    @Bean
    public ThreadPoolTaskExecutor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("event-async-");
        executor.setCorePoolSize(1);
        executor.initialize();
        return executor;
    }

    /** 接口式监听器（XML 时代唯一写法）的注解装配版 */
    @Bean
    public InterfaceEventListener interfaceEventListener() {
        return new InterfaceEventListener();
    }

    /** 容器 i18n 消息解析：bean 名必须是 messageSource 才会被 ApplicationContext 自动采用 */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("message");
        return source;
    }
}
