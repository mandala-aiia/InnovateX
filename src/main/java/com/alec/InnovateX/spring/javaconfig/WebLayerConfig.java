package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 子容器配置（对应传统 SSM 的 servlet-context：Controller 层）：
 * - orderController 的构造参数类型 CustomerService 子容器没有 -> 依赖解析自动向上委托父容器
 * - messageBridge 与父容器同名：子容器自己的定义优先（遮蔽），父容器不受影响
 */
@Configuration
public class WebLayerConfig {

    @Bean
    public OrderController orderController(CustomerService customerService) {
        return new OrderController(customerService);
    }

    @Bean
    public SharedComponent messageBridge() {
        return new SharedComponent("子容器");
    }
}
