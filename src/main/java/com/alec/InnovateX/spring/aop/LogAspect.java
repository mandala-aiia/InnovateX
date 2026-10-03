package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @AspectJ 注解式切面——五种通知全套演示。
 * 可复用的 @Pointcut 方法 + 五种通知的执行顺序（正常返回时）：
 * @Around(前半) -> @Before -> 目标方法 -> @AfterReturning -> @After -> @Around(后半)
 * 异常时：@Around(前半) -> @Before -> 目标方法抛异常 -> @AfterThrowing -> @After
 */
@Aspect
public class LogAspect {

    public static final List<String> LOG = new CopyOnWriteArrayList<>();

    @Pointcut("execution(* com.alec.InnovateX.spring.aop.AopOrderService.*(..))")
    public void orderPointcut() {
    }

    @Before("orderPointcut()")
    public void before(JoinPoint joinPoint) {
        LOG.add("before:" + joinPoint.getSignature().getName());
        System.out.println("[LogAspect] @Before -> " + joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "orderPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        LOG.add("afterReturning:" + result);
        System.out.println("[LogAspect] @AfterReturning <- " + result);
    }

    @AfterThrowing(pointcut = "orderPointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        LOG.add("afterThrowing:" + ex.getClass().getSimpleName());
        System.out.println("[LogAspect] @AfterThrowing <- " + ex.getClass().getSimpleName());
    }

    @After("orderPointcut()")
    public void after(JoinPoint joinPoint) {
        LOG.add("after:" + joinPoint.getSignature().getName());
        System.out.println("[LogAspect] @After（相当于 finally，正常/异常都会走）");
    }

    @Around("orderPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        LOG.add("around:before");
        System.out.println("[LogAspect] @Around 前置");
        try {
            Object result = pjp.proceed();
            LOG.add("around:after");
            System.out.println("[LogAspect] @Around 后置");
            return result;
        } catch (Throwable t) {
            LOG.add("around:after-throwing");
            System.out.println("[LogAspect] @Around 捕获异常后重新抛出");
            throw t;
        }
    }

    /** 让无接口的 AopMessageService 也被增强，用于验证 CGLIB 代理路径 */
    @Before("execution(* com.alec.InnovateX.spring.aop.AopMessageService.send(..))")
    public void beforeSend(JoinPoint joinPoint) {
        LOG.add("beforeSend");
        System.out.println("[LogAspect] @Before -> AopMessageService.send");
    }
}
