package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 满配 Bean 装配：@Bean(initMethod/destroyMethod) 声明 Java Config 侧的生命周期方法
 * （与 XML 的 init-method/destroy-method 属性等价，补齐第三种声明方式）
 */
@Configuration
public class FullLifecycleConfig {

    @Bean
    public LifecycleDependency lifecycleDependency() {
        return new LifecycleDependency();
    }

    @Bean
    public FullLifecycleProcessor fullLifecycleProcessor() {
        return new FullLifecycleProcessor();
    }

    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public FullLifecycleBean fullLifecycleBean() {
        return new FullLifecycleBean();
    }
}
