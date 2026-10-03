package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 通过 @Import 引入 BeanDefinitionRegistryPostProcessor：
 * 它会在所有 Bean 实例化之前执行，动态注册 registryDynamicBean
 */
@Configuration
@Import(JavaRegistryPostProcessor.class)
public class RegistryConfig {
}
