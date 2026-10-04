package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 满配 Bean 装配：用 @Bean(initMethod/destroyMethod) 在装配侧声明生命周期方法，
 * 与 XML 的 init-method/destroy-method 属性、以及 @PostConstruct/@PreDestroy 注解形成三种方式对比——
 * 推荐优先注解（标准 JSR-250）或 initMethod（不侵入类），InitializingBean/DisposableBean 仅在需要
 * 编程式处理时使用。
 */
@Configuration
public class FullChainConfig {

    @Bean
    public ChainDependency chainDependency() {
        return new ChainDependency();
    }

    /**
     * BPP 必须用 static @Bean 注册：容器在"注册后处理器"阶段就要创建它，
     * static 方法无需先实例化本配置类即可调用，避免配置类绕过后处理器被提前创建
     * （完整原因见 extension 包 ExtensionConfig 的注释）。
     */
    @Bean
    public static ChainWatchProcessor chainWatchProcessor() {
        return new ChainWatchProcessor();
    }

    @Bean(initMethod = "manualInit", destroyMethod = "manualDestroy")
    public FullChainBean fullChainBean() {
        return new FullChainBean();
    }
}
