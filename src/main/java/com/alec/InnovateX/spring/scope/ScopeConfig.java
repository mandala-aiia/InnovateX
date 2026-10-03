package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * 作用域与作用域代理：
 * - prototypeTargetBean 用 proxyMode=TARGET_CLASS 声明，注入到单例时给的是 CGLIB 代理
 * - XML 里等价写法是 <bean class="..." scope="prototype"><aop:scoped-proxy/></bean>
 * 注意：demo 故意不用 @ComponentScan，避免同包其他演示配置类被连带扫描进来互相干扰
 */
@Configuration
public class ScopeConfig {

    @Bean
    public SingletonBean singletonBean() {
        return new SingletonBean();
    }

    @Bean
    @Scope("prototype")
    public PrototypeBean prototypeBean() {
        return new PrototypeBean();
    }

    @Bean
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public PrototypeTargetBean prototypeTargetBean() {
        return new PrototypeTargetBean();
    }

    /** @Bean 创建的对象同样会被 AutowiredAnnotationBeanPostProcessor 处理字段注入 */
    @Bean
    public ScopeProxyHolder scopeProxyHolder() {
        return new ScopeProxyHolder();
    }
}
