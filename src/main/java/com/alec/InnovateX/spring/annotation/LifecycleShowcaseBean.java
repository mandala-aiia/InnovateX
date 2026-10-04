package com.alec.InnovateX.spring.annotation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 初始化/销毁全链路顺序演示：一个 Bean 同时用上三种初始化与三种销毁方式，实际回调顺序为
 *   构造器 -> @PostConstruct -> InitializingBean.afterPropertiesSet -> @Bean(initMethod)
 *   关闭时恰好对称反序：@PreDestroy -> DisposableBean.destroy -> @Bean(destroyMethod)。
 * 为什么 @PostConstruct 排最前：它由 CommonAnnotationBeanPostProcessor 驱动，发生在属性注入
 * 之后、"容器级回调"（接口/init-method）之前，适合做只依赖注入结果的轻量校验；
 * init-method 声明在 @Bean 上、与类代码零耦合，是三者中最值得用的扩展位。
 * 静态事件表用 CopyOnWriteArrayList：容器回调与测试断言分处不同阶段，线程安全且随时可读
 */
public class LifecycleShowcaseBean implements InitializingBean, DisposableBean {

    /** 记录回调顺序的共享演示状态（测试用例前统一 clear） */
    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    public LifecycleShowcaseBean() {
        EVENTS.add("1-构造器");
        System.out.println("[LifecycleShowcaseBean] 1-构造器");
    }

    /** JSR-250 注解回调：属性注入完成后立刻执行，时机最早 */
    @PostConstruct
    public void jsr250Init() {
        EVENTS.add("2-@PostConstruct");
        System.out.println("[LifecycleShowcaseBean] 2-@PostConstruct");
    }

    /** 容器级接口回调：与 Spring API 耦合，业务类一般不推荐实现 */
    @Override
    public void afterPropertiesSet() {
        EVENTS.add("3-afterPropertiesSet");
        System.out.println("[LifecycleShowcaseBean] 3-InitializingBean.afterPropertiesSet");
    }

    /** 声明式 init-method：由 @Bean(initMethod="customInit") 指定，解耦最强 */
    public void customInit() {
        EVENTS.add("4-customInit");
        System.out.println("[LifecycleShowcaseBean] 4-@Bean(initMethod)");
    }

    @PreDestroy
    public void jsr250Destroy() {
        EVENTS.add("5-@PreDestroy");
        System.out.println("[LifecycleShowcaseBean] 5-@PreDestroy");
    }

    @Override
    public void destroy() {
        EVENTS.add("6-destroy");
        System.out.println("[LifecycleShowcaseBean] 6-DisposableBean.destroy");
    }

    public void customDestroy() {
        EVENTS.add("7-customDestroy");
        System.out.println("[LifecycleShowcaseBean] 7-@Bean(destroyMethod)");
    }
}
