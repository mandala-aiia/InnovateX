package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义 @Qualifier 元注解：自身被 @Qualifier 标注后，就成了一个"语义化限定符"——
 * - 标在 Bean 类上：给 Bean 定义挂上这个限定符（比裸用 @Qualifier("xxx") 字符串更有编译期检查）
 * - 标在注入点上：只在"挂了同款限定符"的候选 Bean 里挑选
 * 匹配逻辑由 QualifierAnnotationAutowireCandidateResolver 完成，优先级高于 @Primary
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Persistent {
}
