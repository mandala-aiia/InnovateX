package com.alec.InnovateX.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 多切面优先级演示装配（与 AopShowcaseConfig 分开，避免两组切面互相污染命中记录）。
 * 两个 @Aspect Bean 各自带 @Order，容器按优先级把它们排成洋葱：外层先进入后退出，内层后进入先退出。
 */
@Configuration
@EnableAspectJAutoProxy
public class OnionConfig {

    @Bean
    public OnionOuterAspect onionOuterAspect() {
        return new OnionOuterAspect();
    }

    @Bean
    public OnionInnerAspect onionInnerAspect() {
        return new OnionInnerAspect();
    }

    @Bean
    public PipelineService pipelineService() {
        return new PipelineService();
    }
}
