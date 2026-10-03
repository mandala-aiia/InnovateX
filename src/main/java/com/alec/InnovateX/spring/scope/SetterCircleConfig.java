package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * setter 循环依赖：三级缓存默认开启（allowCircularReferences=true）时创建成功；
 * 容器层面关闭后（Boot 3.2+/Spring 6.1 的官方立场）同样抛异常。
 * A/B 用字段注入模拟 setter/属性填充阶段的循环依赖
 */
@Configuration
public class SetterCircleConfig {

    @Bean
    public SetterCircleA setterCircleA() {
        return new SetterCircleA();
    }

    @Bean
    public SetterCircleB setterCircleB() {
        return new SetterCircleB();
    }
}
