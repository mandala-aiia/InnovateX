package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义 @Qualifier 元注解（语义化限定符）：自己被 @Qualifier 元标注后即成为"限定符"——
 * - 标在实现类上：等价于给这个 Bean 挂上一枚名为 @Durable 的标签
 * - 标在注入点（字段/参数）上：只在挂有同款标签的候选 Bean 里挑选
 * 相比裸用 @Qualifier("字符串")：标签拼错在编译期就报错，且类与注入点两端同时受约束。
 * 匹配由 QualifierAnnotationAutowireCandidateResolver 完成，优先级高于 @Primary
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface Durable {
}
