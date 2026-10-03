package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Lazy 修饰"注入点"打破构造器循环依赖：
 * B 构造时注入的不是 A 本尊，而是一个 CGLIB 懒代理（getBeanFactory().getBeanProvider 思路），
 * 第一次真正调用代理方法时才去容器解析 A，此时 A 已构造完毕
 */
@Configuration
public class LazyCircleConfig {

    @Bean
    public LazyPointCircleA lazyPointCircleA() {
        return new LazyPointCircleA();
    }

    @Bean
    public LazyPointCircleB lazyPointCircleB(@Lazy LazyPointCircleA a) {
        return new LazyPointCircleB(a);
    }
}
