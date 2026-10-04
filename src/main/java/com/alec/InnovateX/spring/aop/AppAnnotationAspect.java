package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @annotation 切点：直接匹配"方法上标注了指定注解"的目标方法，
 * 并把注解本体绑定为通知参数（无需再 AnnotationUtils.findAnnotation 反查）。
 * 原来由 XML aop:config 声明切点，注解版直接写表达式
 */
@Aspect
public class AppAnnotationAspect {

    public static final List<String> MATCHED = new CopyOnWriteArrayList<>();

    @Before("@annotation(appAnnotation)")
    public void before(JoinPoint joinPoint, AppAnnotation appAnnotation) {
        String record = "annotation:" + joinPoint.getSignature().getName() + ":" + appAnnotation.value();
        MATCHED.add(record);
        System.out.println("Before @annotation 切点: " + record);
    }
}
