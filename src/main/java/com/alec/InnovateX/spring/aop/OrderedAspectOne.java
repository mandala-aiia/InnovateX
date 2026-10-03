package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/** 切面一（@Order(1)，优先级高）：洋葱模型的外层 */
@Aspect
@Order(1)
public class OrderedAspectOne {

    @Around("execution(* com.alec.InnovateX.spring.aop.MultiAspectService.work(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MultiAspectService.EVENTS.add("aspect1-around-before");
        try {
            return pjp.proceed();
        } finally {
            MultiAspectService.EVENTS.add("aspect1-around-after");
        }
    }
}
