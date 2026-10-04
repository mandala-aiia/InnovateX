package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 切点表达式动物园（除 execution 外的五种匹配维度），靶子是 {@link TradeDeskService}：
 * - within(类型)    按"方法声明所在的类"限定（静态维度，只看代码位置）
 * - target(类型)    按"代理背后的目标对象类型"限定（动态维度，含接口匹配、覆盖子类）
 * - args(类型...)   按"运行期实参的实际类型"匹配——注意不是签名里的声明类型
 * - bean(名称)      Spring 独有扩展，直接按容器里的 bean 名字匹配，支持 * 通配
 * - @annotation(x)  匹配"方法上标注了指定注解"，注解本体还能绑定为通知参数 x
 */
@Aspect
public class PointcutSafariAspect {

    public static final List<String> HITS = new CopyOnWriteArrayList<>();

    @Before("within(com.alec.InnovateX.spring.aop.TradeDeskService)")
    public void byWithin(JoinPoint jp) {
        HITS.add("within:" + jp.getSignature().getName());
        System.out.println("[PointcutSafari] within 命中: " + jp.getSignature().getName());
    }

    @Before("target(com.alec.InnovateX.spring.aop.TradeDeskService)")
    public void byTarget(JoinPoint jp) {
        HITS.add("target:" + jp.getSignature().getName());
        System.out.println("[PointcutSafari] target 命中: " + jp.getSignature().getName());
    }

    /** 只匹配"恰好两个参数：第一个 String、第二个 Integer"的调用 */
    @Before("args(java.lang.String, java.lang.Integer)")
    public void byArgs(JoinPoint jp) {
        HITS.add("args:" + jp.getSignature().getName());
        System.out.println("[PointcutSafari] args 命中: " + jp.getSignature().getName());
    }

    @Before("bean(tradeDeskService)")
    public void byBean(JoinPoint jp) {
        HITS.add("bean:" + jp.getSignature().getName());
        System.out.println("[PointcutSafari] bean 命中: " + jp.getSignature().getName());
    }

    /** @annotation(audited)：形参名 audited 与切点占位符一致，注解实例直接绑定进来 */
    @Before("@annotation(audited)")
    public void byAnnotation(JoinPoint jp, Audited audited) {
        HITS.add("annotation:" + jp.getSignature().getName() + ":" + audited.tag());
        System.out.println("[PointcutSafari] @annotation 命中: " + jp.getSignature().getName()
                + ", tag=" + audited.tag());
    }
}
