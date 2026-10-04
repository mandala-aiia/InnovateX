package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 配套观察用 BeanPostProcessor：只对 FullChainBean 记录前置/后置回调（第 06/10 步）。
 *
 * 顺序教学点（Spring 5.x~7.x 的真实行为，本仓库 7.0.9 实测）：
 * 它的"前置"回调出现在 @PostConstruct 之前——因为 CommonAnnotationBeanPostProcessor
 * 属于 MergedBeanDefinitionPostProcessor，注册收尾时被 PostProcessorRegistrationDelegate
 * "重新注册"到处理器链末尾（addBeanPostProcessor 先移除再添加即等效于挪位），
 * 未实现 Ordered 的自定义 BPP 于是排到了注解处理器前面。
 * 真实框架里，AOP 代理对象正是在第 10 步"后置回调"中生成并替换原对象返回给容器。
 */
public class ChainWatchProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FullChainBean) {
            FullChainBean.TRACE.add("06-自定义BeanPostProcessor.postProcessBeforeInitialization");
            System.out.println("[ChainWatchProcessor] 06 前置回调（@PostConstruct 之前，原因见类注释），bean=" + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FullChainBean) {
            FullChainBean.TRACE.add("10-自定义BeanPostProcessor.postProcessAfterInitialization");
            System.out.println("[ChainWatchProcessor] 10 后置回调（代理一般在此生成），bean=" + beanName);
        }
        return bean;
    }
}
