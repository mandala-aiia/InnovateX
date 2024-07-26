package com.alec.InnovateX;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

public class AppAnnotationAspect {

    public void before(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        AppAnnotation annotation = AnnotationUtils.findAnnotation(methodSignature.getMethod(), AppAnnotation.class);
        System.out.println("Before appPointcutAnnotation: " + annotation.value());
    }
}
