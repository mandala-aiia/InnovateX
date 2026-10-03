package com.alec.InnovateX.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 引介演示配置。proxyTargetClass=true 强制 CGLIB：
 * DocumentService 没有自己的接口，JDK 代理只能代表"被引介的 Lockable"而无法代表目标类型本身，
 * CGLIB 子类代理则同时满足两者（可转型为 DocumentService 也可转型为 Lockable）
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class IntroAopConfig {

    @Bean
    public DocumentService documentService() {
        return new DocumentService();
    }

    @Bean
    public IntroductionAspect introductionAspect() {
        return new IntroductionAspect();
    }
}
