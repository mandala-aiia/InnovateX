package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/** 内层切面（@Order(50)，数值大 = 优先级低 = 洋葱内层）：后进入、先退出 */
@Aspect
@Order(50)
public class OnionInnerAspect {

    @Around("execution(* com.alec.InnovateX.spring.aop.PipelineService.pump(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        PipelineService.FLOW.add("inner(50):enter");
        System.out.println("[OnionInnerAspect] @Order(50) 进入（洋葱内层）");
        try {
            return pjp.proceed();
        } finally {
            PipelineService.FLOW.add("inner(50):exit");
            System.out.println("[OnionInnerAspect] @Order(50) 退出（洋葱内层）");
        }
    }
}
