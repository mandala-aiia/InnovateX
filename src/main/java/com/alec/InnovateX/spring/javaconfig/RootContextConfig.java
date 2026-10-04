package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 父容器配置（对应传统 SSM 的 root-context：数据源/Service 层）：
 * - customerService：只在父容器注册，子容器取它必须"向上委托"
 * - messageBridge：与子容器刻意同名，用于演示子容器对父容器同名 Bean 的遮蔽
 */
@Configuration
public class RootContextConfig {

    @Bean
    public CustomerService customerService() {
        return new CustomerService("父容器");
    }

    @Bean
    public SharedComponent messageBridge() {
        return new SharedComponent("父容器");
    }
}
