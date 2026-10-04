package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 自定义条件：Environment 中 innovatex.feature.enabled=true 才成立。
 * ConditionContext 提供四个抓手——beanFactory / registry（注册表） / environment（属性） /
 * resourceLoader（资源加载），因此条件可以基于"容器状态 + 环境属性 + 类路径资源"任意组合判断
 */
public class OnTogglePropertyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("innovatex.feature.enabled");
        boolean match = "true".equalsIgnoreCase(enabled);
        System.out.println("[OnTogglePropertyCondition] innovatex.feature.enabled=" + enabled + "，匹配=" + match);
        return match;
    }
}
