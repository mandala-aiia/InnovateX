package com.alec.InnovateX.spring.extension;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * BFPP：所有 BeanDefinition 就绪之后、任何普通单例实例化之前回调，
 * 是修改"定义"层面的最后机会。
 * XML 时代的玩法是 BeanDefinitionVisitor 逐个改写 <property> 的字符串值；
 * 注解侧 @Bean 的属性值写在 Java 代码里、不在 Definition 中，因此这里演示
 * 直接改 Definition 元数据——把 extensionDemoBean 从急切创建改成懒加载
 */
public class AppBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("AppBeanFactoryPostProcessor..........postProcessBeanFactory");
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("extensionDemoBean");
        beanDefinition.setLazyInit(true);
        System.out.println("BFPP 已把 extensionDemoBean 改为 lazy-init=" + beanDefinition.isLazyInit());
    }
}
