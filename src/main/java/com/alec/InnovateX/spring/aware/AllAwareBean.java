package com.alec.InnovateX.spring.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringValueResolver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Aware 全家桶：容器在 Bean 初始化阶段回调这些 setXxx 接口，把基础设施对象"塞给"Bean。
 * 底层由 ApplicationContextAwareProcessor（一个 BeanPostProcessor）统一驱动，
 * 回调发生在 @PostConstruct 之前。对比现有 spring 包已演示的 BeanNameAware/ApplicationContextAware：
 * - BeanFactoryAware：拿到底层 BeanFactory（getBean 等底层 API）
 * - EnvironmentAware：拿到 Environment（profile、PropertySource 遍历）
 * - ApplicationEventPublisherAware：拿到事件发布器（不依赖 ApplicationContext 也能发事件）
 * - MessageSourceAware：拿到国际化消息源
 * - ResourceLoaderAware：拿到资源加载器（classpath:/file: 等资源定位）
 * - EmbeddedValueResolverAware：拿到内嵌值解析器（解析 ${} 占位符，@Value 的底层机制）
 */
public class AllAwareBean implements BeanFactoryAware, EnvironmentAware, ApplicationEventPublisherAware,
        MessageSourceAware, ResourceLoaderAware, EmbeddedValueResolverAware {

    public static final List<String> CALLBACK_ORDER = new CopyOnWriteArrayList<>();

    private BeanFactory beanFactory;

    private Environment environment;

    private ApplicationEventPublisher eventPublisher;

    private MessageSource messageSource;

    private ResourceLoader resourceLoader;

    private StringValueResolver embeddedValueResolver;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        CALLBACK_ORDER.add("BeanFactoryAware");
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        CALLBACK_ORDER.add("EnvironmentAware");
        this.environment = environment;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        CALLBACK_ORDER.add("ApplicationEventPublisherAware");
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        CALLBACK_ORDER.add("MessageSourceAware");
        this.messageSource = messageSource;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        CALLBACK_ORDER.add("ResourceLoaderAware");
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        CALLBACK_ORDER.add("EmbeddedValueResolverAware");
        this.embeddedValueResolver = resolver;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ApplicationEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public StringValueResolver getEmbeddedValueResolver() {
        return embeddedValueResolver;
    }
}
