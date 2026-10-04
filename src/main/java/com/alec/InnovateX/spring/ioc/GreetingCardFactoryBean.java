package com.alec.InnovateX.spring.ioc;

import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean 演示：注册到容器的是「工厂」，getBean 拿到的却是 getObject() 的「产品」。
 * - getBean("greetingCard") → GreetingCard 产品
 * - getBean("&greetingCard") → 工厂自身（& 前缀）
 * - isSingleton()=true → 产品会被缓存，getObject() 只调一次
 * 大量集成场景（MyBatis 的 MapperFactoryBean 等）都基于这个机制往容器里塞复杂对象。
 */
public class GreetingCardFactoryBean implements FactoryBean<GreetingCard> {

    private String prefix = "你好";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public GreetingCard getObject() {
        return new GreetingCard(prefix + ", Spring 7");
    }

    @Override
    public Class<?> getObjectType() {
        return GreetingCard.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
