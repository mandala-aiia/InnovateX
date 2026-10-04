package com.alec.InnovateX.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP 演示配置：
 * - @EnableAspectJAutoProxy 开启注解驱动 AOP（注册 AnnotationAwareAspectJAutoProxyCreator）
 * - exposeProxy = true：把当前代理放进 AopContext 的 ThreadLocal，自调用场景可以显式取回代理
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopConfig {

    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    @Bean
    public PointcutZooAspect pointcutZooAspect() {
        return new PointcutZooAspect();
    }

    @Bean
    public AppAnnotationAspect appAnnotationAspect() {
        return new AppAnnotationAspect();
    }

    @Bean
    public SelfInvokeAspect selfInvokeAspect() {
        return new SelfInvokeAspect();
    }

    @Bean
    public AopOrderService aopOrderService() {
        return new AopOrderServiceImpl();
    }

    @Bean
    public AopMessageService aopMessageService() {
        return new AopMessageService();
    }

    @Bean
    public ZooTargetService zooTargetService() {
        return new ZooTargetService();
    }

    @Bean
    public SelfInvokeService selfInvokeService() {
        return new SelfInvokeService();
    }
}
