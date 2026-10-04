package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * full 模式（@Configuration 的默认行为）：配置类被 CGLIB 增强为代理，
 * 类内调用 @Bean 方法（storageClient() 里调 storageEngine()）会被拦截并转发到容器——
 * 结果：无论"看似调了几次"，方法体只真实执行一次，全容器共享同一个单例。
 * 代价：每个配置类都要生成代理子类，且代理要求 @Bean 方法非 private/final
 */
@Configuration
public class FullProxyConfig {

    @Bean
    public StorageEngine storageEngine() {
        return new StorageEngine("full-storageEngine");
    }

    @Bean
    public StorageClient storageClient() {
        // 看似普通方法直调，实际被代理拦截 -> 返回容器里名为 storageEngine 的那个单例
        return new StorageClient(storageEngine());
    }
}
