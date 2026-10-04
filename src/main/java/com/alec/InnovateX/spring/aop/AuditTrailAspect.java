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
 * 五种通知全套切面。Spring 对同一切面内的通知按"种类"排序织入（@Around 最外、@Before 次之），
 * 因此无论方法声明顺序如何，运行顺序恒为：
 *
 * 正常路径：@Around 前段 -&gt; @Before -&gt; 目标方法 -&gt; @AfterReturning -&gt; @After -&gt; @Around 后段
 * 异常路径：@Around 前段 -&gt; @Before -&gt; 目标方法抛异常 -&gt; @AfterThrowing -&gt; @After -&gt; @Around 抛出
 *
 * 教学要点：@After 是 finally 语义（两条路径都执行）；@AfterReturning 与 @AfterThrowing 二选一；
 * @Around 是唯一能"改写调用本身"的通知（可以吞异常、换返回值、甚至不调目标方法）。
 */
@Aspect
public class AuditTrailAspect {

    /** 演示状态：线程安全 CopyOnWriteArrayList，测试用它做精确顺序断言 */
    public static final List<String> LOG = new CopyOnWriteArrayList<>();

    /** 可复用切点：接口上所有方法——JDK 代理只会拦接口方法，切点写接口类型最稳妥 */
    @Pointcut("execution(* com.alec.InnovateX.spring.aop.PaymentChannel.*(..))")
    public void channelOps() {
    }

    /** 第二个切点：命中无接口的 SmsNotifier，验证 CGLIB 路线同样被增强 */
    @Pointcut("execution(* com.alec.InnovateX.spring.aop.SmsNotifier.send(..))")
    public void smsOps() {
    }

    @Around("channelOps()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        LOG.add("around-enter");
        System.out.println("[AuditTrailAspect] @Around 前段: " + pjp.getSignature().getName());
        try {
            Object result = pjp.proceed();
            LOG.add("around-exit");
            System.out.println("[AuditTrailAspect] @Around 后段（拿到返回值后放行）");
            return result;
        } catch (Throwable t) {
            LOG.add("around-rethrow");
            System.out.println("[AuditTrailAspect] @Around 捕获异常后原样抛出: " + t.getClass().getSimpleName());
            throw t;
        }
    }

    @Before("channelOps()")
    public void before(JoinPoint jp) {
        LOG.add("before:" + jp.getSignature().getName());
        System.out.println("[AuditTrailAspect] @Before -> " + jp.getSignature().getName());
    }

    /** returning = "result" 把目标方法返回值绑定为通知参数（参数名必须一致） */
    @AfterReturning(pointcut = "channelOps()", returning = "result")
    public void afterReturning(JoinPoint jp, Object result) {
        LOG.add("afterReturning:" + result);
        System.out.println("[AuditTrailAspect] @AfterReturning <- " + result);
    }

    /** throwing = "ex" 把目标方法抛出的异常绑定为通知参数；目标方法正常返回时本通知不执行 */
    @AfterThrowing(pointcut = "channelOps()", throwing = "ex")
    public void afterThrowing(JoinPoint jp, Throwable ex) {
        LOG.add("afterThrowing:" + ex.getClass().getSimpleName());
        System.out.println("[AuditTrailAspect] @AfterThrowing <- " + ex.getClass().getSimpleName());
    }

    @After("channelOps()")
    public void after(JoinPoint jp) {
        LOG.add("after:" + jp.getSignature().getName());
        System.out.println("[AuditTrailAspect] @After（finally 语义，正常/异常都执行）-> "
                + jp.getSignature().getName());
    }

    @Before("smsOps()")
    public void beforeSms(JoinPoint jp) {
        LOG.add("sms-before:" + jp.getSignature().getName());
        System.out.println("[AuditTrailAspect] @Before -> SmsNotifier.send（CGLIB 路线同样生效）");
    }
}
