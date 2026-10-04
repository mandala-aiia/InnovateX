package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 注册自定义作用域并让 Bean 使用它。
 * <p>
 * 两个关键细节：
 * <ul>
 *   <li>CustomScopeConfigurer 是 BeanFactoryPostProcessor——它必须在所有普通 Bean 创建
 *       <b>之前</b>把 "thread" 作用域登记进 BeanFactory，所以声明为 static @Bean：
 *       static 方法不依赖配置类实例，容器无需提前实例化配置类本身，避免"BFPP 晚到"的告警；</li>
 *   <li>@Scope("thread") 引用的名字必须与 configurer 登记的名字完全一致，
 *       否则容器找不到作用域会抛 IllegalStateException（"No Scope SPI registered"）。</li>
 * </ul>
 */
@Configuration
public class ThreadScopeConfig {

    /** 暴露作用域实例，供教学代码直接观察（常规业务不需要这样做） */
    public static final ThreadScope THREAD_SCOPE = new ThreadScope();

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(ThreadScope.SCOPE_NAME, THREAD_SCOPE);
        return configurer;
    }

    @Bean
    @Scope(ThreadScope.SCOPE_NAME)
    public TraceTag traceTag() {
        return new TraceTag();
    }
}
