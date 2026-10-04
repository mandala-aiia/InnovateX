package com.alec.InnovateX.spring.extension;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor（BPP）：围绕"初始化"环节的扩展点——
 *   postProcessBeforeInitialization：初始化回调之前（@PostConstruct / afterPropertiesSet / initMethod 前）；
 *   postProcessAfterInitialization：全部初始化完成之后，容器拿到的最终对象。
 * 对比 IABPP（管"造出来"），BPP 管"初始化好"；Spring 注解驱动的能力大多寄生在 BPP 上
 * （@PostConstruct 的 CommonAnnotationBeanPostProcessor、AOP 代理的 AbstractAutoProxyCreator 等）。
 */
public class InitializationAuditor implements BeanPostProcessor {

    private static boolean watching(String beanName) {
        return "onDemandService".equals(beanName);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (watching(beanName)) {
            ExtensionTimeline.TIMELINE.add("bpp:postProcessBeforeInitialization");
            System.out.println("[InitializationAuditor] 初始化之前（@PostConstruct 等回调将随后执行）");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (watching(beanName)) {
            ExtensionTimeline.TIMELINE.add("bpp:postProcessAfterInitialization");
            System.out.println("[InitializationAuditor] 初始化之后（AOP 代理一般在此生成并替换返回值）");
        }
        return bean;
    }
}
