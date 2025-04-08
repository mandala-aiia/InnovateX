package com.alec.InnovateX.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.*;

public class AppBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("AppBeanFactoryPostProcessor..........postProcessBeanFactory");
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("appDev");

        BeanDefinitionVisitor beanDefinitionVisitor = new BeanDefinitionVisitor(strVal -> {
            if (strVal.equals("原神Dev")) {
                return "原神DevName";
            }
            return strVal;
        });
        beanDefinitionVisitor.visitBeanDefinition(beanDefinition);
    }
}
