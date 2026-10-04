package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 外层切面（@Order(5)，数值小 = 优先级高 = 洋葱外层）：先进入、后退出。
 * 对比内层 OnionInnerAspect 的 @Order(50)。@Order 不写则优先级最低（Ordered.LOWEST_PRECEDENCE）。
 */
@Aspect
@Order(5)
public class OnionOuterAspect {

    @Around("execution(* com.alec.InnovateX.spring.aop.PipelineService.pump(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        PipelineService.FLOW.add("outer(5):enter");
        System.out.println("[OnionOuterAspect] @Order(5) 进入（洋葱外层）");
        try {
            return pjp.proceed();
        } finally {
            PipelineService.FLOW.add("outer(5):exit");
            System.out.println("[OnionOuterAspect] @Order(5) 退出（洋葱外层）");
        }
    }
}
