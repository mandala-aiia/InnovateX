package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 构造器注入循环依赖：三级缓存只能拯救"先实例化、后填充属性"的注入方式，
 * 构造器注入连第一步实例化都完不成，refresh 阶段直接抛 BeanCurrentlyInCreationException
 */
@Configuration
public class ConstructorCircleConfig {

    @Bean
    public ConstructorCircleA constructorCircleA(ConstructorCircleB b) {
        return new ConstructorCircleA(b);
    }

    @Bean
    public ConstructorCircleB constructorCircleB(ConstructorCircleA a) {
        return new ConstructorCircleB(a);
    }
}
