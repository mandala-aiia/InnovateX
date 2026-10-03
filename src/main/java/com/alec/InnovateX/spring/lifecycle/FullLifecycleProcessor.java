package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 配套 BPP：只对 FullLifecycleBean 记录前置/后置回调。
 * 注意它没有实现 Ordered/PriorityOrdered，排在 CommonAnnotationBeanPostProcessor 等
 * 注解处理器之后——所以事件 7（BPP 前置）出现在 @PostConstruct 之后
 */
public class FullLifecycleProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FullLifecycleBean) {
            FullLifecycleBean.EVENTS.add("7.BeanPostProcessor.beforeInitialization");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FullLifecycleBean) {
            FullLifecycleBean.EVENTS.add("BeanPostProcessor.afterInitialization（初始化完成，代理一般在此生成）");
        }
        return bean;
    }
}
