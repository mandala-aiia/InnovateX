package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 注册自定义作用域：
 * CustomScopeConfigurer 本身是个 BeanFactoryPostProcessor，必须声明为 static @Bean 让它提前实例化
 */
@Configuration
public class ThreadScopeConfig {

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(ThreadScope.SCOPE_NAME, new ThreadScope());
        return configurer;
    }

    @Bean
    @Scope(ThreadScope.SCOPE_NAME)
    public ThreadScopedBean threadScopedBean() {
        return new ThreadScopedBean();
    }
}
