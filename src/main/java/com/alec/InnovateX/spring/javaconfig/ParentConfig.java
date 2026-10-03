package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 父容器配置（对应传统 SSM 的 root-context：数据源、Service 层）：
 * - parentOnlyService：只在父容器定义
 * - configService：与子容器"同名"，用于演示子容器对父容器同名 bean 的遮蔽
 */
@Configuration
public class ParentConfig {

    @Bean
    public SharedService parentOnlyService() {
        return new SharedService("父容器");
    }

    @Bean
    public SharedService configService() {
        return new SharedService("父容器");
    }
}
