package com.alec.InnovateX.spring.aware;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Aware 的现代替代写法：容器本身（ApplicationContext/Environment/ObjectProvider 等）
 * 就是可以被直接注入的依赖 —— 优先用构造器注入拿容器能力，少用 Aware 回调。
 */
public class AltAccessBean {

    private final ApplicationContext context;

    private final ObjectProvider<Environment> environmentProvider;

    public AltAccessBean(ApplicationContext context, ObjectProvider<Environment> environmentProvider) {
        this.context = context;
        this.environmentProvider = environmentProvider;
    }

    public String beanNameOf(Class<?> type) {
        return context.getBeanNamesForType(type)[0];
    }

    public Environment environment() {
        return environmentProvider.getObject();
    }
}
