package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * full 模式（@Configuration 默认）：配置类会被 CGLIB 增强为代理，
 * 类内调用 @Bean 方法会被拦截并转发到容器，因此 orderDao() 里调 orderDataSource()
 * 拿到的是容器中的同一个单例，即使 orderDataSource() 方法体里 new 了新对象也只执行一次。
 */
@Configuration
public class FullModeConfig {

    @Bean
    public OrderDataSource orderDataSource() {
        return new OrderDataSource("full-mode");
    }

    @Bean
    public OrderDao orderDao() {
        // 看似调用了两次 orderDataSource()，实际被 CGLIB 拦截，返回容器内同一个 Bean
        return new OrderDao(orderDataSource());
    }
}
