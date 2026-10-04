package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Proxy;

/**
 * 替换型 BeanPostProcessor：postProcessAfterInitialization 返回 JDK 动态代理替换原 bean。
 * AOP 的代理生成（AbstractAutoProxyCreator）正是挂在这同一个钩子上。
 */
public class WrappingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if ("lifeWrapped".equals(beanName) && bean instanceof GreeterService target) {
            GreeterService proxy = (GreeterService) Proxy.newProxyInstance(
                    GreeterService.class.getClassLoader(),
                    new Class<?>[] {GreeterService.class},
                    (p, method, args) -> method.invoke(target, args) + "[被BPP代理]");
            LifecycleLog.record("BPP替换:lifeWrapped");
            return proxy;
        }
        return bean;
    }
}
