package com.alec.InnovateX.spring.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @Profile 分环境注册：同一类型按激活的 profile 只保留一个实现，
 * 一个都不激活时按类型取 bean 会抛 NoSuchBeanDefinitionException。
 */
@Configuration
public class StorageConfig {

    @Bean
    @Profile("dev")
    public Storage devStorage() {
        return new DevStorage();
    }

    @Bean
    @Profile("prod")
    public Storage prodStorage() {
        return new ProdStorage();
    }
}
