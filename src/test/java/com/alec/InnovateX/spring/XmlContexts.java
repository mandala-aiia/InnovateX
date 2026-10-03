package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.extension.AppReaderEventListener;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * XML demo 容器的共享加载入口：各主题测试类拆分验证 SpringCodeTest 内容时复用，
 * 保持与原始 demo 完全一致的装载方式（XmlBeanDefinitionReader + ReaderEventListener）
 */
public final class XmlContexts {

    private XmlContexts() {
    }

    public static GenericApplicationContext load() {
        return load(new AppReaderEventListener());
    }

    public static GenericApplicationContext load(AppReaderEventListener listener) {
        Resource resource = new ClassPathResource("spring-context.xml");
        GenericApplicationContext context = new GenericApplicationContext();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        reader.setEventListener(listener);
        reader.loadBeanDefinitions(resource);
        context.refresh();
        return context;
    }
}
