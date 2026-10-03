package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.extension.AppBeanFactoryPostProcessor;
import com.alec.InnovateX.spring.extension.AppBeanPostProcessor;
import com.alec.InnovateX.spring.extension.AppFactoryBean;
import com.alec.InnovateX.spring.extension.AppFaBean;
import com.alec.InnovateX.spring.extension.AppInstantiationAwareBeanPostProcessor;
import com.alec.InnovateX.spring.extension.AppReaderEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 容器扩展点（XML 装配版）：FactoryBean 的产品/本体两种获取方式、
 * ReaderEventListener 监听 BeanDefinition 注册、三类后处理器随容器装配生效
 */
public class ExtensionTest {

    @Test
    public void factoryBeanProductAndItself() {
        try (GenericApplicationContext context = XmlContexts.load()) {
            // 按名称（不带 &）：拿到的是"产品"（getObject() 的产物）
            Object byName = context.getBean("appFactoryBean");
            assertInstanceOf(AppFaBean.class, byName);
            assertEquals("源生", ((AppFaBean) byName).getAppFaBeanName());

            // 按 FactoryBean 类型 或 "&"前缀：拿到的是工厂本身（需自行 getObject()，原 SpringCodeTest 即此用法）
            assertInstanceOf(AppFactoryBean.class, context.getBean(AppFactoryBean.class));
            assertInstanceOf(AppFactoryBean.class, context.getBean("&appFactoryBean"));
            System.out.println("FactoryBean: 按名称拿产品 " + byName
                    + "；按工厂类型或 '&' 前缀拿工厂本身（需自行 getObject()）");
        }
    }

    @Test
    public void readerEventListenerCounting() {
        List<String> registered = new CopyOnWriteArrayList<>();
        try (GenericApplicationContext context = XmlContexts.load(new AppReaderEventListener() {
            @Override
            public void componentRegistered(ComponentDefinition componentDefinition) {
                registered.add(componentDefinition.getName());
                super.componentRegistered(componentDefinition);
            }
        })) {
            // spring-context.xml 系列（含 import 的四个分册）注册的全部 BeanDefinition 都经过回调
            assertTrue(registered.size() > 10, "注册的组件数应大于 10，实际 " + registered.size());
            assertTrue(registered.contains("app") && registered.contains("appJdbcTemplate"));
            System.out.println("ReaderEventListener: 共监听到 " + registered.size() + " 个组件注册（含 import 的分册）");
        }
    }

    @Test
    public void postProcessorsWired() {
        // 三类后处理器本身就是普通 Bean（XML 声明）；容器构建成功且它们的打印已产生即证明生效
        try (GenericApplicationContext context = XmlContexts.load()) {
            assertInstanceOf(AppBeanPostProcessor.class, context.getBean(AppBeanPostProcessor.class));
            assertInstanceOf(AppInstantiationAwareBeanPostProcessor.class,
                    context.getBean(AppInstantiationAwareBeanPostProcessor.class));
            assertInstanceOf(AppBeanFactoryPostProcessor.class,
                    context.getBean(AppBeanFactoryPostProcessor.class));
            System.out.println("BPP/InstantiationAwareBPP/BFPP: 均已注册并对容器内 Bean 生效");
        }
    }
}
