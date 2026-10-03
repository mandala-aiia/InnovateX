package com.alec.InnovateX.spring.javaconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * BeanDefinitionRegistryPostProcessor：比 BeanFactoryPostProcessor 更底层的扩展点。
 * 执行时机（refresh 主流程）：
 * 1. invokeBeanFactoryPostProcessors 先执行所有 BDRPP 的 postProcessBeanDefinitionRegistry
 * 2. 再执行 BDRPP/BFPP 的 postProcessBeanFactory
 * 此时所有 BeanDefinition 刚加载完、任何 Bean 还未实例化，最适合"追加/删除/改写 BeanDefinition"
 */
public class JavaRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("[JavaRegistryPostProcessor] postProcessBeanDefinitionRegistry：动态注册 registryDynamicBean");
        registry.registerBeanDefinition("registryDynamicBean",
                BeanDefinitionBuilder.genericBeanDefinition(RegistryDynamicBean.class).getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("[JavaRegistryPostProcessor] postProcessBeanFactory：此时已有 beanDefinition 数量="
                + beanFactory.getBeanDefinitionCount());
    }
}
