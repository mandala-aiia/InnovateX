package com.alec.InnovateX.spring.aop;

/** 有接口的目标类：Spring AOP 默认对它生成 JDK 动态代理 */
public interface AopOrderService {

    String createOrder(String orderNo);

    String cancelOrder(String orderNo);
}
