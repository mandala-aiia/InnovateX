package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 全链路生命周期 bean：构造 → Aware → BPP 前置 → @PostConstruct → afterPropertiesSet → BPP 后置
 * →（容器关闭）DABPP 销毁前 → @PreDestroy → DisposableBean.destroy。
 * 只有 BPP 前置/后置之间的三步算「初始化回调」，顺序固定。
 */
public class FullLifecycleBean implements BeanNameAware, InitializingBean, DisposableBean {

    public FullLifecycleBean() {
        LifecycleLog.record("构造");
    }

    @Override
    public void setBeanName(String name) {
        LifecycleLog.record("Aware:beanName=" + name);
    }

    @PostConstruct
    public void annotationInit() {
        LifecycleLog.record("@PostConstruct");
    }

    @Override
    public void afterPropertiesSet() {
        LifecycleLog.record("InitializingBean.afterPropertiesSet");
    }

    @PreDestroy
    public void annotationShutdown() {
        LifecycleLog.record("@PreDestroy");
    }

    @Override
    public void destroy() {
        LifecycleLog.record("DisposableBean.destroy");
    }
}
