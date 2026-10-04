package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 通过 @Import 引入 BeanDefinitionRegistryPostProcessor：
 * 它会在任何 Bean 实例化之前被回调，动态注册 runtimeGiftBean
 */
@Configuration
@Import(DynamicRegistrarProcessor.class)
public class RegistryProcessorConfig {
}
