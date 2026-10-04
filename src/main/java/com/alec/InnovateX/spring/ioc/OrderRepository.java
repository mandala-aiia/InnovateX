package com.alec.InnovateX.spring.ioc;

/**
 * 订单仓储：不带任何 Spring 注解的普通类，专供「手动注册 / BeanDefinition API」演示。
 * 带 source 属性便于观察属性注入与 BeanDefinition 的 propertyValues。
 */
public class OrderRepository {

    private String source = "默认构造";

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public int count() {
        return 42;
    }
}
