package com.alec.InnovateX.spring.component;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义组合注解（meta-annotation）：自身标了 @Component，
 * 因此标注它的类会被组件扫描识别，@Repository/@Service 本质就是这样派生的。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BizComponent {

    /** 与 @Component.value 互通，可当 bean 名用。 */
    @AliasFor(annotation = Component.class)
    String value() default "";
}
