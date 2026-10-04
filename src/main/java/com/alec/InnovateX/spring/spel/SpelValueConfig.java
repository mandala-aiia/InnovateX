package com.alec.InnovateX.spring.spel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * SpEL 演示配置：@Bean 声明式装配 + 复用 annotation/annotation-app.properties 作为占位符来源。
 * SpelValueHolder 的 @Value 字段演示 #{}（SpEL）与 ${}（占位符）的各种混用姿势
 */
@Configuration
@PropertySource("classpath:annotation/annotation-app.properties")
public class SpelValueConfig {

    /** 被 #{@seedGenerator...} 引用的目标 Bean：SpEL 里用 @bean 名 直接引用容器里的 Bean */
    @Bean
    public SeedGenerator seedGenerator() {
        return new SeedGenerator(100);
    }

    @Bean
    public SpelValueHolder spelValueHolder() {
        return new SpelValueHolder();
    }
}
