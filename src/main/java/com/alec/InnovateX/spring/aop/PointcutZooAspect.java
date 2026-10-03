package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** execution 之外的切点表达式演示：within / target / args / bean */
@Aspect
public class PointcutZooAspect {

    public static final List<String> MATCHED = new CopyOnWriteArrayList<>();

    @Before("within(com.alec.InnovateX.spring.aop.ZooTargetService)")
    public void withinPointcut(JoinPoint jp) {
        MATCHED.add("within:" + jp.getSignature().getName());
    }

    @Before("target(com.alec.InnovateX.spring.aop.ZooTargetService)")
    public void targetPointcut(JoinPoint jp) {
        MATCHED.add("target:" + jp.getSignature().getName());
    }

    /** 只匹配第一个参数是 String、第二个参数是 Integer 的方法 */
    @Before("args(java.lang.String, java.lang.Integer)")
    public void argsPointcut(JoinPoint jp) {
        MATCHED.add("args:" + jp.getSignature().getName());
    }

    /** Spring 特有：按 bean 名称匹配（bean 名称支持 * 通配） */
    @Before("bean(zooTargetService)")
    public void beanPointcut(JoinPoint jp) {
        MATCHED.add("bean:" + jp.getSignature().getName());
    }
}
