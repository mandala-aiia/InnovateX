package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * 记录型 BeanPostProcessor：在初始化回调前后各记一笔。
 * 实现 PriorityOrdered + 最高优先级，保证记录发生在 @PostConstruct/@PreDestroy（由低优先级的
 * CommonAnnotationBeanPostProcessor 执行）之前，从而能断言出完整顺序。
 */
public class RecordingBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    /** 只记录 life 开头的演示 bean；排除嵌套配置类（其 bean 名形如 xxx.YyyConfig，同样会经过 BPP）。 */
    private static boolean isDemoBean(String beanName) {
        return beanName.startsWith("life") && !beanName.contains(".");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (isDemoBean(beanName)) {
            LifecycleLog.record("BPP前置:" + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (isDemoBean(beanName)) {
            LifecycleLog.record("BPP后置:" + beanName);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
