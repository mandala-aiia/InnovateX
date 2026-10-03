package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 子容器配置（对应传统 SSM 的 servlet-context：Controller 层）：
 * - childService 的构造参数在子容器没有定义，注入时向上委托父容器
 * - configService 与父容器同名：子容器的 getBean 优先命中自己的定义（遮蔽父容器）
 */
@Configuration
public class ChildConfig {

    @Bean
    public ChildService childService(SharedService parentOnlyService) {
        return new ChildService(parentOnlyService);
    }

    @Bean
    public SharedService configService() {
        return new SharedService("子容器");
    }
}
