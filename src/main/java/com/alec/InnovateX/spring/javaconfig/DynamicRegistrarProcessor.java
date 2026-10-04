package com.alec.InnovateX.spring.javaconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * BeanDefinitionRegistryPostProcessor：比 BeanFactoryPostProcessor 更底层的扩展点。
 * 执行时机（refresh 主流程 invokeBeanFactoryPostProcessors 阶段）：
 * 1. 先执行所有 BDRPP 的 postProcessBeanDefinitionRegistry（可增删改 BeanDefinition）
 * 2. 再执行 BDRPP/BFPP 的 postProcessBeanFactory（只能改 BeanFactory 属性）
 * 此时所有 BeanDefinition 刚加载完、任何 Bean 还未实例化——动态注册的最佳时机
 */
public class DynamicRegistrarProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("[DynamicRegistrarProcessor] postProcessBeanDefinitionRegistry：动态注册 runtimeGiftBean");
        registry.registerBeanDefinition("runtimeGiftBean",
                BeanDefinitionBuilder.genericBeanDefinition(RuntimeGiftBean.class).getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("[DynamicRegistrarProcessor] postProcessBeanFactory：此刻 BeanDefinition 总数="
                + beanFactory.getBeanDefinitionCount());
    }
}
