package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * DestructionAwareBeanPostProcessor：在 bean 销毁回调（@PreDestroy / destroyMethod）之前介入，
 * 这是 AOP 代理清理、资源释放的官方钩子。
 */
public class RecordingDestructionBPP implements DestructionAwareBeanPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        if (beanName.startsWith("life") && !beanName.contains(".")) {
            LifecycleLog.record("DABPP销毁前:" + beanName);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
