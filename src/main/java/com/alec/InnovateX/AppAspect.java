package com.alec.InnovateX;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public class AppAspect {

    public void before(JoinPoint joinPoint) {
        System.out.println("AppAspect Before Method: " + joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName());
    }

    public void afterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("AppAspect After Returning Method: " + joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + ":" + result);
    }

    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        System.out.println("AppAspect After Throwing Method: " + joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + ":" + ex.getMessage());
    }

    public Object around(ProceedingJoinPoint joinPoint) {
        System.out.println("执行在" + joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "之前");
        try {
            joinPoint.proceed();
        } catch (Throwable e) {
            //
        }
        System.out.println("执行在" + joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "之后");
        return null;
    }

    public void after(JoinPoint joinPoint) {
        System.out.println("AppAspect After Method: " + joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName());
    }
}
