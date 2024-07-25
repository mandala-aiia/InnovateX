package com.alec.InnovateX;

import org.springframework.beans.factory.BeanNameAware;

public class AppBeanNameAware implements BeanNameAware {
    @Override
    public void setBeanName(String name) {
        System.out.println("AppBeanNameAware"+"................................"+name);
    }
}
