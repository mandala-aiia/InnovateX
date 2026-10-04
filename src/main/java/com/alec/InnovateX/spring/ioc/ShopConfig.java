package com.alec.InnovateX.spring.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 最小 @Configuration 配置类：@Bean 方法返回值即 bean。
 * 两个计算器标了 @Lazy —— refresh 结束后并不实例化，首次 getBean 才创建，
 * 配合静态计数器可以直观看到「列举名字不实例化、getBeansOfType 才实例化」。
 */
@Configuration
public class ShopConfig {

    @Bean
    public OrderRepository orderRepository() {
        return new OrderRepository();
    }

    @Bean
    public OrderService orderService(OrderRepository orderRepository) {
        return new OrderService(orderRepository);
    }

    @Bean
    @Lazy
    public RegularPriceCalculator regularPriceCalculator() {
        return new RegularPriceCalculator();
    }

    @Bean
    @Lazy
    public VipPriceCalculator vipPriceCalculator() {
        return new VipPriceCalculator();
    }
}
