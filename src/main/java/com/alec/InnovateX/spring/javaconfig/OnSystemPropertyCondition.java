package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 自定义条件：系统属性 javaconfig.enabled=true 时成立（matches 返回 true）
 * Condition 能拿到 ConditionContext（beanFactory/environment/resourceLoader），判断维度很丰富
 */
public class OnSystemPropertyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("javaconfig.enabled");
        boolean match = "true".equalsIgnoreCase(enabled);
        System.out.println("[OnSystemPropertyCondition] javaconfig.enabled=" + enabled + "，匹配结果: " + match);
        return match;
    }
}
