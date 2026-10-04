package com.alec.InnovateX.spring.javaconfig;

/** 满足 @Conditional 条件时才会被注册的 Bean */
public class ToggleGuardedBean {

    public String describe() {
        return "条件成立，我才会被注册进容器";
    }
}
