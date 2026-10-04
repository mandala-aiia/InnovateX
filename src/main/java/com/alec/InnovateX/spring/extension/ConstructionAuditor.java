package com.alec.InnovateX.spring.extension;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

/**
 * InstantiationAwareBeanPostProcessor（IABPP）：BeanPostProcessor 的"实例化阶段"增强版，
 * 回调点比普通 BPP 更早——围绕"构造对象"与"填充属性"两个环节（而 BPP 围绕"初始化"环节）：
 *
 *   postProcessBeforeInstantiation：构造器执行【前】。返回非 null 可直接短路容器创建
 *       （AOP 的 TargetSource 就是用这个口子替换目标对象），这里返回 null 走正常创建；
 *   postProcessAfterInstantiation：构造器执行【后】、属性填充前。返回 false 可否决属性填充；
 *   postProcessProperties：属性应用【前】最后加工 PropertyValues
 *       （@Autowired 的真正执行者 AutowiredAnnotationBeanPostProcessor 就是 IABPP）。
 *
 * 本类只观察 onDemandService 一个 Bean，把三个回调写入时间线，与 InitializationAuditor
 * 的记录放在一起即可看出两类后处理器各自的"管辖区间"。
 */
public class ConstructionAuditor implements InstantiationAwareBeanPostProcessor {

    private static boolean watching(String beanName) {
        return "onDemandService".equals(beanName);
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (watching(beanName)) {
            ExtensionTimeline.TIMELINE.add("iabpp:postProcessBeforeInstantiation");
            System.out.println("[ConstructionAuditor] 实例化之前（返回 null = 交给容器正常创建）");
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (watching(beanName)) {
            ExtensionTimeline.TIMELINE.add("iabpp:postProcessAfterInstantiation");
            System.out.println("[ConstructionAuditor] 实例化之后（返回 true = 继续属性填充）");
        }
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        if (watching(beanName)) {
            ExtensionTimeline.TIMELINE.add("iabpp:postProcessProperties");
            System.out.println("[ConstructionAuditor] 属性填充之前（@Autowired 的执行者就在这一步）");
        }
        return pvs;
    }
}
