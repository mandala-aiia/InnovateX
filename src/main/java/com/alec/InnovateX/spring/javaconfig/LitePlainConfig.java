package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * lite 模式（proxyBeanMethods = false）：配置类不生成 CGLIB 代理，
 * 类内调用 @Bean 方法就是普通 Java 方法调用——每次都执行方法体、每次都 new 新实例。
 * Spring Boot 的大量自动配置类都用 lite 模式省去代理开销，代价是 @Bean 方法之间不能互相引用
 */
@Configuration(proxyBeanMethods = false)
public class LitePlainConfig {

    @Bean
    public StorageEngine storageEngine() {
        return new StorageEngine("lite-storageEngine");
    }

    @Bean
    public StorageClient storageClient() {
        // 普通方法直调：这里 new 出来的 StorageEngine 与容器中的那个是两个不同实例
        return new StorageClient(storageEngine());
    }
}
