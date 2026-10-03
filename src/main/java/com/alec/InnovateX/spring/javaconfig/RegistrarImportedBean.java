package com.alec.InnovateX.spring.javaconfig;

/** 由 ImportBeanDefinitionRegistrar 手动注册的 Bean */
public class RegistrarImportedBean {

    public String hello() {
        return "我是被 ImportBeanDefinitionRegistrar 手动注册的 Bean";
    }
}
