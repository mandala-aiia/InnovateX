package com.alec.InnovateX.spring.ioc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 自定义 BeanFactoryPostProcessor：在所有单例实例化之前，改写 greeter 的 BeanDefinition，
 * 追加 message 属性（属性注入发生在构造之后，因此会覆盖构造器里赋的值）。
 * 演示点：ApplicationContext 会在 refresh 阶段自动执行 BFPP；裸 BeanFactory 必须手动调用。
 */
public class GreeterCustomizingBFPP implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory.containsBeanDefinition("greeter")) {
            BeanDefinition definition = beanFactory.getBeanDefinition("greeter");
            definition.getPropertyValues().add("message", "被 BFPP 修改过的问候");
        }
    }
}
