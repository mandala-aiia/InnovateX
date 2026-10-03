package com.alec.InnovateX.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/** 多切面优先级演示装配：@Order 值小的切面在外层（先进入后退出，洋葱模型） */
@Configuration
@EnableAspectJAutoProxy
public class MultiAspectConfig {

    @Bean
    public OrderedAspectOne orderedAspectOne() {
        return new OrderedAspectOne();
    }

    @Bean
    public OrderedAspectTwo orderedAspectTwo() {
        return new OrderedAspectTwo();
    }

    @Bean
    public MultiAspectService multiAspectService() {
        return new MultiAspectService();
    }
}
