package com.alec.InnovateX.spring.javaconfig;

/** 由 ImportBeanDefinitionRegistrar 手工注册的 Bean（bean 名、元数据都由注册代码决定） */
public class RegistrarCraftedBean {

    public String describe() {
        return "我是被 ImportBeanDefinitionRegistrar 手工注册的 Bean";
    }
}
