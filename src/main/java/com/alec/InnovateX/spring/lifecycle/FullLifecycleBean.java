package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * "满配 Bean"：把 Bean 生命周期的所有回调打在同一个 Bean 上，按真实执行顺序记录成事件链。
 * 初始化阶段（AbstractAutowireCapableBeanFactory 的 doCreateBean 流程）：
 *   1.构造器实例化 -> 2.属性填充(@Autowired setter) -> 3/4.invokeAwareMethods(BeanName/BeanFactory)
 *   -> 5.BPP 前置循环里的 ApplicationContextAwareProcessor(ApplicationContext) 与 @PostConstruct
 *   -> 自定义 BPP 前置 -> InitializingBean.afterPropertiesSet -> @Bean(initMethod)
 *   -> 自定义 BPP 后置
 * 销毁阶段（DisposableBeanAdapter 的顺序）：
 *   @PreDestroy -> DisposableBean.destroy -> @Bean(destroyMethod)
 */
public class FullLifecycleBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware,
        InitializingBean, DisposableBean {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    public FullLifecycleBean() {
        EVENTS.add("1.构造器实例化");
    }

    /** 属性填充阶段注入依赖（发生在任何初始化回调之前） */
    @Autowired
    public void setDependency(LifecycleDependency dependency) {
        EVENTS.add("2.@Autowired setter注入");
    }

    @Override
    public void setBeanName(String name) {
        EVENTS.add("3.BeanNameAware.setBeanName");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        EVENTS.add("4.BeanFactoryAware.setBeanFactory");
    }

    /** 由 BPP 前置循环中的 ApplicationContextAwareProcessor 回调 */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EVENTS.add("5.ApplicationContextAware.setApplicationContext");
    }

    /** CommonAnnotationBeanPostProcessor 在 BPP 前置循环中触发 */
    @PostConstruct
    public void onPostConstruct() {
        EVENTS.add("6.@PostConstruct");
    }

    @Override
    public void afterPropertiesSet() {
        EVENTS.add("8.InitializingBean.afterPropertiesSet");
    }

    /** 由 @Bean(initMethod = "customInit") 声明 */
    public void customInit() {
        EVENTS.add("9.@Bean(initMethod)自定义初始化");
    }

    @PreDestroy
    public void onPreDestroy() {
        EVENTS.add("10.@PreDestroy");
    }

    @Override
    public void destroy() {
        EVENTS.add("11.DisposableBean.destroy");
    }

    /** 由 @Bean(destroyMethod = "customDestroy") 声明（@Bean 默认还会推断 close/shutdown 方法） */
    public void customDestroy() {
        EVENTS.add("12.@Bean(destroyMethod)自定义销毁");
    }
}
