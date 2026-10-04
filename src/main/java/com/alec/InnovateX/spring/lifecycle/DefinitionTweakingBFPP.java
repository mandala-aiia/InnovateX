package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * BFPP：在「所有单例实例化之前」改写 BeanDefinition（这里给 lifeLegacy 补一个 owner 属性）。
 * 注意执行时机：比 BeanPostProcessor 更早，比 bean 实例化更早 —— 这也是它只能改定义、不能改实例的原因。
 */
public class DefinitionTweakingBFPP implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LifecycleLog.record("BFPP执行");
        if (beanFactory.containsBeanDefinition("lifeLegacy")) {
            BeanDefinition definition = beanFactory.getBeanDefinition("lifeLegacy");
            definition.getPropertyValues().add("owner", "BFPP注入的主人");
        }
    }
}
