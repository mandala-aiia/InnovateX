package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 解法一/二：注入 ObjectProvider / ObjectFactory（两者的「查找」句柄）而非实例本身，
 * 每次调用 getObject() 才向容器要新对象。
 */
public class ProviderBoss {

    private final ObjectProvider<PrototypeProduct> provider;

    private final ObjectFactory<PrototypeProduct> factory;

    @Autowired
    public ProviderBoss(ObjectProvider<PrototypeProduct> provider, ObjectFactory<PrototypeProduct> factory) {
        this.provider = provider;
        this.factory = factory;
    }

    public int nextFromProvider() {
        return provider.getObject().id();
    }

    public int nextFromFactory() {
        return factory.getObject().id();
    }
}
