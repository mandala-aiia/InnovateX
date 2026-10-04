package com.alec.InnovateX.spring.component;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 自定义 Condition：环境属性 deploy.env=prod 时 @Conditional 标注的 bean 才注册。
 * Spring Boot 的各种 @ConditionalOnXxx 底层就是这套 Condition 机制。
 */
public class ProdOnlyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return "prod".equals(context.getEnvironment().getProperty("deploy.env"));
    }
}
