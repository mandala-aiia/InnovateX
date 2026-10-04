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
 * "满配 Bean"：把 Bean 生命周期全部回调打到同一个 Bean 上，按真实执行顺序记录成 13 步事件链。
 * 本类负责其中 11 步（第 07/10 两步由配套的 ChainWatchProcessor 记录）。
 *
 * 初始化阶段（AbstractAutowireCapableBeanFactory.doCreateBean 的固定流程）：
 *   01 构造器实例化 → 02 属性填充 → 03/04 invokeAwareMethods(BeanName/BeanFactory)
 *   → 05~07 BeanPostProcessor 前置循环（ApplicationContextAwareProcessor 回调 ApplicationContextAware、
 *      自定义 BPP 前置、CommonAnnotationBeanPostProcessor 触发 @PostConstruct）
 *   → 08/09 invokeInitMethods(afterPropertiesSet → @Bean initMethod)
 *   → 10 BeanPostProcessor 后置循环（AOP 代理通常在此生成）
 * 销毁阶段（DisposableBeanAdapter 的固定顺序，与初始化大体反序）：
 *   11 @PreDestroy → 12 DisposableBean.destroy → 13 @Bean destroyMethod
 *
 * 顺序细节（重要教学点）：自定义 BPP 前置(06)在 @PostConstruct(07) 之前——
 * 因为 CommonAnnotationBeanPostProcessor 属于 MergedBeanDefinitionPostProcessor，
 * 会被 PostProcessorRegistrationDelegate 在注册收尾时"挪到处理器链末尾"，
 * 于是未实现 Ordered 的自定义 BPP 反而排到了注解处理器前面。
 *
 * 教学对比点：本类"特意"用 @Autowired setter 注入，为的是把"构造"与"属性填充"拆成两步观察；
 * 业务代码首选仍是构造器注入（final 字段 + 显式构造器），见本包 ComputeLifecycle 的示范。
 */
public class FullChainBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware,
        InitializingBean, DisposableBean {

    /** 13 步完整事件链（编号前缀保证可读性与顺序可断言） */
    public static final List<String> TRACE = new CopyOnWriteArrayList<>();

    private ChainDependency dependency;

    public FullChainBean() {
        TRACE.add("01-构造器实例化");
        System.out.println("[FullChainBean] 01 构造器：实例诞生，但属性和依赖都还没有");
    }

    /** 属性填充阶段（populateBean）：发生在任何 Aware / 初始化回调之前 */
    @Autowired
    public void injectDependency(ChainDependency dependency) {
        TRACE.add("02-属性填充：@Autowired setter 注入");
        this.dependency = dependency;
        System.out.println("[FullChainBean] 02 属性填充：注入 " + dependency);
    }

    @Override
    public void setBeanName(String name) {
        TRACE.add("03-BeanNameAware.setBeanName");
        System.out.println("[FullChainBean] 03 BeanNameAware：我的名字是 " + name);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        TRACE.add("04-BeanFactoryAware.setBeanFactory");
        System.out.println("[FullChainBean] 04 BeanFactoryAware");
    }

    /** 由 BeanPostProcessor 前置循环里的 ApplicationContextAwareProcessor 回调（非 invokeAwareMethods） */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TRACE.add("05-ApplicationContextAware.setApplicationContext");
        System.out.println("[FullChainBean] 05 ApplicationContextAware");
    }

    /** CommonAnnotationBeanPostProcessor 在 BeanPostProcessor 前置循环中触发（见类注释的顺序细节） */
    @PostConstruct
    public void annotationInit() {
        TRACE.add("07-@PostConstruct");
        System.out.println("[FullChainBean] 07 @PostConstruct：注解声明的初始化钩子");
    }

    @Override
    public void afterPropertiesSet() {
        TRACE.add("08-InitializingBean.afterPropertiesSet");
        System.out.println("[FullChainBean] 08 afterPropertiesSet：接口声明的初始化（侵入式，耦合 Spring API）");
    }

    /** 由装配侧 @Bean(initMethod = "manualInit") 声明：第三种初始化方式，不侵入类本身 */
    public void manualInit() {
        TRACE.add("09-@Bean(initMethod=manualInit)");
        System.out.println("[FullChainBean] 09 自定义 initMethod：装配侧声明的初始化");
    }

    @PreDestroy
    public void annotationDestroy() {
        TRACE.add("11-@PreDestroy");
        System.out.println("[FullChainBean] 11 @PreDestroy：注解声明的销毁钩子");
    }

    @Override
    public void destroy() {
        TRACE.add("12-DisposableBean.destroy");
        System.out.println("[FullChainBean] 12 DisposableBean.destroy：接口声明的销毁");
    }

    /** 由装配侧 @Bean(destroyMethod = "manualDestroy") 声明 */
    public void manualDestroy() {
        TRACE.add("13-@Bean(destroyMethod=manualDestroy)");
        System.out.println("[FullChainBean] 13 自定义 destroyMethod：装配侧声明的销毁");
    }
}
