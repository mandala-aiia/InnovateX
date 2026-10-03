package com.alec.InnovateX.spring.scope;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 三级缓存验证器：SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference
 * 是三级缓存的出口——只有当某个"正在创建中"的 Bean 被别人需要时（循环依赖），
 * 容器才会从第三级缓存 singletonFactories 里取出工厂并调用本方法拿到"早期引用"。
 * 真实项目中这个回调由 AOP 的 AbstractAutoProxyCreator 实现：直接返回提前生成的代理，
 * 这就是"AOP 的 Bean 发生循环依赖时，注入的是代理而不是原始对象"的原因。
 */
public class EarlyReferenceProcessor implements SmartInstantiationAwareBeanPostProcessor {

    /** 记录哪些 Bean 走过三级缓存的早期曝光 */
    public static final List<String> EARLY_REFERENCES = new CopyOnWriteArrayList<>();

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        EARLY_REFERENCES.add(beanName);
        System.out.println("[三级缓存] getEarlyBeanReference 被调用: " + beanName
                + "（此时 " + beanName + " 还在创建中，尚非完整 Bean）");
        // 这里原样返回；AOP 场景下 AnnotationAwareAspectJAutoProxyCreator 会在这里返回代理
        return bean;
    }
}
