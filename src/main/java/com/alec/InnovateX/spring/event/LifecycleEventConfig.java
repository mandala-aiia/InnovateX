package com.alec.InnovateX.spring.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 内置事件演示装配（独立配置：只挂生命周期记录器，测试断言的时间线干净无杂音） */
@Configuration
public class LifecycleEventConfig {

    @Bean
    public ContextLifecycleRecorder contextLifecycleRecorder() {
        return new ContextLifecycleRecorder();
    }
}
