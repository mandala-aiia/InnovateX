package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.extension.AppBeanFactoryPostProcessor;
import com.alec.InnovateX.spring.extension.AppBeanPostProcessor;
import com.alec.InnovateX.spring.extension.AppFactoryBean;
import com.alec.InnovateX.spring.extension.AppFaBean;
import com.alec.InnovateX.spring.extension.AppInstantiationAwareBeanPostProcessor;
import com.alec.InnovateX.spring.extension.ExtensionConfig;
import com.alec.InnovateX.spring.extension.ExtensionDemoBean;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 容器扩展点（注解装配版）：FactoryBean 的产品/本体两种获取方式、
 * BFPP 在实例化前改写 BeanDefinition、三类后处理器随容器装配生效
 */
public class ExtensionTest {

    @Test
    public void factoryBeanProductAndItself() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ExtensionConfig.class)) {
            // 按名称（不带 &）：拿到的是"产品"（getObject() 的产物）
            Object byName = ctx.getBean("appFactoryBean");
            assertInstanceOf(AppFaBean.class, byName);
            assertEquals("源生", ((AppFaBean) byName).getAppFaBeanName());

            // 按 FactoryBean 类型 或 "&"前缀：拿到的是工厂本身（需自行 getObject()）
            assertInstanceOf(AppFactoryBean.class, ctx.getBean(AppFactoryBean.class));
            assertInstanceOf(AppFactoryBean.class, ctx.getBean("&appFactoryBean"));
            System.out.println("FactoryBean: 按名称拿产品 " + byName
                    + "；按工厂类型或 '&' 前缀拿工厂本身（需自行 getObject()）");
        }
    }

    @Test
    public void beanFactoryPostProcessorRewritesDefinition() {
        ExtensionDemoBean.instantiated = false;
        // BFPP 在"所有定义就绪之后、单例实例化之前"把 extensionDemoBean 改成了 lazy：
        // refresh 结束它不该被创建，第一次 getBean 才构造
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ExtensionConfig.class)) {
            assertFalse(ExtensionDemoBean.instantiated, "BFPP 改写 lazy 后 refresh 不应实例化目标 Bean");
            ExtensionDemoBean bean = ctx.getBean(ExtensionDemoBean.class);
            assertTrue(ExtensionDemoBean.instantiated);
            System.out.println("BFPP 改写 Definition 生效: refresh 后 lazy，getBean 时才构造 "
                    + bean.getClass().getSimpleName());
        }
    }

    @Test
    public void postProcessorsWired() {
        // 三类后处理器本身就是普通 Bean（static @Bean 声明）；容器构建成功且它们的打印已产生即证明生效
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ExtensionConfig.class)) {
            assertInstanceOf(AppBeanPostProcessor.class, ctx.getBean(AppBeanPostProcessor.class));
            assertInstanceOf(AppInstantiationAwareBeanPostProcessor.class,
                    ctx.getBean(AppInstantiationAwareBeanPostProcessor.class));
            assertInstanceOf(AppBeanFactoryPostProcessor.class,
                    ctx.getBean(AppBeanFactoryPostProcessor.class));
            System.out.println("BPP/InstantiationAwareBPP/BFPP: 均已注册并对容器内 Bean 生效");
        }
    }
}
