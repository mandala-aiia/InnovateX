package com.alec.InnovateX.spring.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 容器扩展点的注解装配版：
 * - FactoryBean 本体/产品两种获取方式（& 前缀）
 * - BFPP/BPP/InstantiationAwareBPP 三类后处理器必须用 static @Bean 注册——
 *   static 让容器无需提前实例化本配置类就能创建它们，否则配置类会先于
 *   后处理器被创建，破坏"后处理器先于普通 Bean 生效"的时序
 */
@Configuration
public class ExtensionConfig {

    @Bean
    public AppFactoryBean appFactoryBean() {
        return new AppFactoryBean();
    }

    /** BFPP 的改写目标：默认急切创建，BFPP 回调里被改成 lazy */
    @Bean
    public ExtensionDemoBean extensionDemoBean() {
        return new ExtensionDemoBean();
    }

    @Bean
    public static AppBeanFactoryPostProcessor appBeanFactoryPostProcessor() {
        return new AppBeanFactoryPostProcessor();
    }

    @Bean
    public static AppBeanPostProcessor appBeanPostProcessor() {
        return new AppBeanPostProcessor();
    }

    @Bean
    public static AppInstantiationAwareBeanPostProcessor appInstantiationAwareBeanPostProcessor() {
        return new AppInstantiationAwareBeanPostProcessor();
    }
}
