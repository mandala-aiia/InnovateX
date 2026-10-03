package com.alec.InnovateX.spring.javaconfig;

/** 由 BeanDefinitionRegistryPostProcessor 动态注册的 Bean */
public class RegistryDynamicBean {

    public String hello() {
        return "我是被 BeanDefinitionRegistryPostProcessor 在 BeanDefinition 加载阶段注册的 Bean";
    }
}
