package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

/**
 * @DependsOn 与 @Lazy：
 * - 两个 Bean 之间没有直接注入关系时，用 @DependsOn 控制初始化顺序（销毁顺序自动反过来）
 * - @Lazy 让 Bean 的创建推迟到第一次使用（getBean / 被其他 Bean 注入），容器启动时只注册定义
 * 注意：dependsOnSecond 声明在前，但没有 @DependsOn 时 Spring 不保证初始化顺序；
 * 加上 @DependsOn("dependsOnFirst") 后容器必须先创建 dependsOnFirst
 */
@Configuration
public class DependsOnConfig {

    @Bean
    @DependsOn("dependsOnFirst")
    public DependsOnSecond dependsOnSecond() {
        return new DependsOnSecond();
    }

    @Bean
    public DependsOnFirst dependsOnFirst() {
        return new DependsOnFirst();
    }

    @Bean
    @Lazy
    public LazyBean lazyBean() {
        return new LazyBean();
    }
}
