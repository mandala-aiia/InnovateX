package com.alec.InnovateX.spring.annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 注解驱动装配的入口配置（两大入口注解 + 一处声明式装配对照）：
 * - @ComponentScan：默认扫描本配置类所在包，凡标注 @Component/@Service/@Repository/@Controller
 *   （或被它们元标注）的类都会被注册为 BeanDefinition，bean 名默认取类名首字母小写
 * - @PropertySource：把 classpath:annotation/annotation-app.properties 追加进 Environment 的
 *   PropertySource 链，之后 @Value("${...}") 占位符才有值可解析
 * - lifecycleShowcaseBean 用 @Bean(initMethod/destroyMethod) 注册：本主题以扫描装配为主，
 *   借这一个声明式 Bean 对照"四种初始化回调的完整顺序"（详见 LifecycleShowcaseBean）
 */
@Configuration
@ComponentScan
@PropertySource("classpath:annotation/annotation-app.properties")
public class AnnotationWiringConfig {

    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleShowcaseBean lifecycleShowcaseBean() {
        return new LifecycleShowcaseBean();
    }
}
