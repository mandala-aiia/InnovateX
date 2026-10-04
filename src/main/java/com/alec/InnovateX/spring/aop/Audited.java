package com.alec.InnovateX.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义方法级注解：配合 @annotation 切点使用。
 * Retention 必须是 RUNTIME——切点在运行期通过反射读取注解，CLASS/ SOURCE 级别切面根本看不见。
 * @annotation(audited) 这种写法还能把注解本体直接绑定为通知参数，省去 AnnotationUtils 反查。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /** 审计标记：绑定进通知参数后可直接读取，用于断言"拿到的是方法上那个注解实例" */
    String tag() default "";
}
