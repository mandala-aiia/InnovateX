package com.alec.InnovateX.spring.scope;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/** 命中 AopCircleA.hello() 的简单切面：迫使容器为 A 生成 CGLIB 代理 */
@Aspect
public class EarlyRefAspect {

    @Before("execution(* com.alec.InnovateX.spring.scope.AopCircleA.hello(..))")
    public void beforeHello(JoinPoint joinPoint) {
        System.out.println("[EarlyRefAspect] 拦截到: " + joinPoint.getSignature().toShortString());
    }
}
