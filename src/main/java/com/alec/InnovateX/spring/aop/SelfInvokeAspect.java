package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/** 增强目标：SelfInvokeService.inner——只有"经过代理"的调用才会被拦到 */
@Aspect
public class SelfInvokeAspect {

    @Before("execution(* com.alec.InnovateX.spring.aop.SelfInvokeService.inner(..))")
    public void beforeInner(JoinPoint joinPoint) {
        SelfInvokeService.LOG.add("inner-advised");
        System.out.println("[SelfInvokeAspect] 拦截到经过代理的 inner() 调用");
    }
}
