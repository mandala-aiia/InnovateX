package com.alec.InnovateX.spring.annotation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * JSR-250 生命周期注解（由 CommonAnnotationBeanPostProcessor 驱动）：
 * - @PostConstruct：属性注入完成后、Bean 投入使用前执行一次
 * - @PreDestroy：容器关闭销毁 Bean 前执行一次（仅 singleton 生效，prototype 不回调）
 * 对比 XML 的 init-method/destroy-method 和 InitializingBean 接口，三者执行顺序：
 * 构造器 -> @PostConstruct -> InitializingBean.afterPropertiesSet -> init-method
 */
@Component
public class AnnotationLifecycleBean {

    private boolean initialized;

    private boolean destroyed;

    /** 供容器关闭后（Bean 已不可访问）从类层面验证 @PreDestroy 已执行 */
    public static volatile boolean destroyedFlag = false;

    @PostConstruct
    public void onPostConstruct() {
        this.initialized = true;
        System.out.println("① @PostConstruct 执行：Bean 初始化完成，initialized=" + initialized);
    }

    @PreDestroy
    public void onPreDestroy() {
        this.destroyed = true;
        destroyedFlag = true;
        System.out.println("② @PreDestroy 执行：Bean 即将销毁，destroyed=" + destroyed);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
