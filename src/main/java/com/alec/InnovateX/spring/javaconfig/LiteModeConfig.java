package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * lite 模式（proxyBeanMethods = false）：配置类不被 CGLIB 代理，
 * 类内调用 @Bean 方法就是普通 Java 方法调用——每次都执行方法体、生成新实例。
 * Spring Boot 大量使用该模式以省去代理开销，代价是方法间不能互相引用。
 */
@Configuration(proxyBeanMethods = false)
public class LiteModeConfig {

    @Bean
    public OrderDataSource orderDataSource() {
        return new OrderDataSource("lite-mode");
    }

    @Bean
    public OrderDao orderDao() {
        // 普通方法调用：这里 new 出来的 OrderDataSource 与容器中的是两个不同实例
        return new OrderDao(orderDataSource());
    }
}
