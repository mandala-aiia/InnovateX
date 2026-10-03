package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 三级缓存 + AOP 场景：
 * 创建 aopCircleA → 属性填充需要 B → 创建 aopCircleB → B 需要 A →
 * 触发 A 的早期曝光 getEarlyBeanReference → AOP 基础设施在那里提前生成代理 →
 * B 拿到的是 A 的代理；A 完成初始化后把代理放回一级缓存
 */
@Configuration
@EnableAspectJAutoProxy
public class AopCircleConfig {

    @Bean
    public EarlyReferenceProcessor earlyReferenceProcessor() {
        return new EarlyReferenceProcessor();
    }

    @Bean
    public EarlyRefAspect earlyRefAspect() {
        return new EarlyRefAspect();
    }

    @Bean
    public AopCircleA aopCircleA() {
        return new AopCircleA();
    }

    @Bean
    public AopCircleB aopCircleB() {
        return new AopCircleB();
    }
}
