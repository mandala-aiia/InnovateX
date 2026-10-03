package com.alec.InnovateX.spring.javaconfig;

/** 满足 @Conditional 条件时才会注册的 Bean */
public class ConditionalBean {

    public String hello() {
        return "条件成立，我被注册进容器了";
    }
}
