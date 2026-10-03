package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/** 切面二（@Order(2)，优先级低）：洋葱模型的内层 */
@Aspect
@Order(2)
public class OrderedAspectTwo {

    @Around("execution(* com.alec.InnovateX.spring.aop.MultiAspectService.work(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MultiAspectService.EVENTS.add("aspect2-around-before");
        try {
            return pjp.proceed();
        } finally {
            MultiAspectService.EVENTS.add("aspect2-around-after");
        }
    }
}
