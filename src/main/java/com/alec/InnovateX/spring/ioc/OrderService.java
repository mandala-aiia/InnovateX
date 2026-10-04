package com.alec.InnovateX.spring.ioc;

/**
 * 订单服务：唯一构造器依赖 OrderRepository。
 * 无论是 @Bean 方法、registerBean 还是 XML，容器都会自动解析这个构造器参数。
 */
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public String summary() {
        return "订单数=" + orderRepository.count();
    }
}
