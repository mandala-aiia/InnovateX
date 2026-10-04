package com.alec.InnovateX.spring.ioc;

/**
 * 问候器：同时提供无参构造、带参构造和 setter，
 * 用于演示构造参数注入 / 属性注入 / getBean 传构造参数 / BeanDefinition 的 propertyValues。
 */
public class Greeter {

    private String message;

    public Greeter() {
        this.message = "默认问候";
    }

    public Greeter(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
