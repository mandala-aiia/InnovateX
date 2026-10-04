package com.alec.InnovateX.spring.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringValueResolver;

/**
 * 集齐 8 个常用 Aware 接口：容器在初始化回调之前把自身能力「推」给 bean。
 * 触发顺序分两批：
 * 1) 容器核心三件套（BeanFactory 内部直接调用）：BeanNameAware → BeanClassLoaderAware → BeanFactoryAware
 * 2) 上下文能力（经 ApplicationContextAwareProcessor）：EnvironmentAware → EmbeddedValueResolverAware
 *    → ResourceLoaderAware → ApplicationEventPublisherAware → ApplicationContextAware
 */
public class AllAwareBean implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware,
        EnvironmentAware, EmbeddedValueResolverAware,
        ResourceLoaderAware, ApplicationEventPublisherAware, ApplicationContextAware, InitializingBean {

    private String beanName;

    private ClassLoader classLoader;

    private BeanFactory beanFactory;

    private Environment environment;

    private StringValueResolver valueResolver;

    private ResourceLoader resourceLoader;

    private ApplicationEventPublisher eventPublisher;

    private ApplicationContext applicationContext;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        AwareLog.record("BeanNameAware");
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        AwareLog.record("BeanClassLoaderAware");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        AwareLog.record("BeanFactoryAware");
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        AwareLog.record("EnvironmentAware");
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.valueResolver = resolver;
        AwareLog.record("EmbeddedValueResolverAware");
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        AwareLog.record("ResourceLoaderAware");
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
        AwareLog.record("ApplicationEventPublisherAware");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        AwareLog.record("ApplicationContextAware");
    }

    @Override
    public void afterPropertiesSet() {
        AwareLog.record("afterPropertiesSet（Aware 全部就绪后才轮到初始化回调）");
    }

    public String getBeanName() {
        return beanName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String resolve(String placeholder) {
        return valueResolver.resolveStringValue(placeholder);
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void publish(String message) {
        eventPublisher.publishEvent(message);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
