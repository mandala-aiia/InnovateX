package com.alec.InnovateX.spring.javaconfig;

/** 由 BeanDefinitionRegistryPostProcessor 在 BeanDefinition 加载阶段动态注册的 Bean */
public class RuntimeGiftBean {

    public String describe() {
        return "我是被 BeanDefinitionRegistryPostProcessor 在所有 Bean 实例化之前注册的";
    }
}
