package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.aop.AliPayChannel;
import com.alec.InnovateX.spring.aop.AopShowcaseConfig;
import com.alec.InnovateX.spring.aop.AuditTrailAspect;
import com.alec.InnovateX.spring.aop.ContractPaperService;
import com.alec.InnovateX.spring.aop.FinalRateTable;
import com.alec.InnovateX.spring.aop.IntroductionConfig;
import com.alec.InnovateX.spring.aop.OnionConfig;
import com.alec.InnovateX.spring.aop.PaymentChannel;
import com.alec.InnovateX.spring.aop.PipelineService;
import com.alec.InnovateX.spring.aop.PointcutSafariAspect;
import com.alec.InnovateX.spring.aop.Sealable;
import com.alec.InnovateX.spring.aop.SmsNotifier;
import com.alec.InnovateX.spring.aop.TradeDeskService;
import com.alec.InnovateX.spring.aop.TransferTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AOP 深度：@AspectJ 五种通知两条路径的精确顺序、JDK vs CGLIB 代理、final 类边界、
 * 编程式 ProxyFactory、Advised 运行时增删通知、切点表达式动物园、自调用失效与 currentProxy 修复、
 * @DeclareParents 引介增强、多切面 @Order 洋葱模型。
 */
public class AopDeepTest {

    @Test
    public void fiveAdviceOrderOnNormalAndExceptionPath() {
        AuditTrailAspect.LOG.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopShowcaseConfig.class)) {
            PaymentChannel channel = ctx.getBean(PaymentChannel.class);

            // 正常返回：around前 -> before -> 目标方法 -> afterReturning -> after -> around后
            assertEquals("pay:SO-9101:99.5", channel.pay("SO-9101", 99.5));
            assertEquals(List.of(
                    "around-enter",
                    "before:pay",
                    "afterReturning:pay:SO-9101:99.5",
                    "after:pay",
                    "around-exit"), AuditTrailAspect.LOG);
            System.out.println("正常路径通知顺序: " + AuditTrailAspect.LOG);

            // 异常路径：around前 -> before -> 目标抛异常 -> afterThrowing -> after(finally) -> around抛出
            AuditTrailAspect.LOG.clear();
            assertThrows(IllegalArgumentException.class, () -> channel.refund(" "));
            assertEquals(List.of(
                    "around-enter",
                    "before:refund",
                    "afterThrowing:IllegalArgumentException",
                    "after:refund",
                    "around-rethrow"), AuditTrailAspect.LOG);
            System.out.println("异常路径通知顺序: " + AuditTrailAspect.LOG);
        }
    }

    @Test
    public void jdkProxyVersusCglibProxy() {
        AuditTrailAspect.LOG.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopShowcaseConfig.class)) {
            // 有接口 -> JDK 动态代理（对接口生成 $Proxy 子类）
            PaymentChannel channel = ctx.getBean(PaymentChannel.class);
            assertTrue(AopUtils.isJdkDynamicProxy(channel), "有接口的目标应走 JDK 动态代理");
            assertFalse(AopUtils.isCglibProxy(channel));
            System.out.println("JDK 代理类型: " + channel.getClass().getName());

            // 无接口 -> CGLIB 子类代理（类名带 $$SpringCGLIB$$）
            SmsNotifier notifier = ctx.getBean(SmsNotifier.class);
            assertTrue(AopUtils.isCglibProxy(notifier), "无接口的目标应走 CGLIB");
            assertFalse(notifier instanceof PaymentChannel);
            notifier.send("SO-9102");
            assertTrue(AuditTrailAspect.LOG.contains("sms-before:send"), "CGLIB 路线同样被切面增强");
            System.out.println("CGLIB 代理类型: " + notifier.getClass().getName());
        }
    }

    @Test
    public void programmaticProxyFactoryAndFinalClassLimit() {
        // 1) 编程式：显式声明接口 -> JDK 代理（MethodBeforeAdvice 是函数式接口，可用 lambda）
        ProxyFactory jdkFactory = new ProxyFactory(new AliPayChannel());
        jdkFactory.addInterface(PaymentChannel.class);
        jdkFactory.addAdvice((MethodBeforeAdvice) (method, args, target) ->
                System.out.println("[ProxyFactory-JDK] 拦截: " + method.getName()));
        PaymentChannel jdkProxy = (PaymentChannel) jdkFactory.getProxy();
        assertTrue(AopUtils.isJdkDynamicProxy(jdkProxy));
        System.out.println("编程式 JDK 代理返回: " + jdkProxy.pay("SO-9201", 1.0));

        // 2) 编程式：无接口可声明 -> 自动退回 CGLIB
        ProxyFactory cglibFactory = new ProxyFactory(new SmsNotifier());
        cglibFactory.addAdvice((MethodBeforeAdvice) (method, args, target) ->
                System.out.println("[ProxyFactory-CGLIB] 拦截: " + method.getName()));
        SmsNotifier cglibProxy = (SmsNotifier) cglibFactory.getProxy();
        assertTrue(AopUtils.isCglibProxy(cglibProxy));
        System.out.println("编程式 CGLIB 代理返回: " + cglibProxy.send("SO-9202"));

        // 3) final 类无法被 CGLIB 继承 -> getProxy() 当场抛异常（Cannot subclass final class）
        ProxyFactory finalFactory = new ProxyFactory(new FinalRateTable());
        assertThrows(Exception.class, finalFactory::getProxy);
        System.out.println("final 类创建代理: 失败（Cannot subclass final class FinalRateTable）");
    }

    @Test
    public void pointcutExpressionSafari() {
        PointcutSafariAspect.HITS.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopShowcaseConfig.class)) {
            TradeDeskService desk = ctx.getBean(TradeDeskService.class);

            desk.withinDemo();
            assertTrue(PointcutSafariAspect.HITS.contains("within:withinDemo"));

            desk.targetDemo();
            assertTrue(PointcutSafariAspect.HITS.contains("target:targetDemo"));

            desk.argsDemo("SKU-1", 3);
            assertTrue(PointcutSafariAspect.HITS.contains("args:argsDemo"));
            // args 按实参类型序列匹配：无参方法不会被 args(String, Integer) 命中
            assertFalse(PointcutSafariAspect.HITS.contains("args:withinDemo"));

            desk.beanDemo();
            assertTrue(PointcutSafariAspect.HITS.contains("bean:beanDemo"));

            // @annotation 切点：注解本体绑定为通知参数，tag 值原样拿到
            assertEquals("annotationDemo", desk.annotationDemo());
            assertTrue(PointcutSafariAspect.HITS.contains("annotation:annotationDemo:desk-audit"));

            System.out.println("切点动物园命中记录: " + PointcutSafariAspect.HITS);
        }
    }

    @Test
    public void selfInvocationBrokenThenFixedByCurrentProxy() {
        TransferTicketService.BOOKINGS.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopShowcaseConfig.class)) {
            TransferTicketService proxy = ctx.getBean(TransferTicketService.class);

            // this.record(...)：目标对象内部直调，不经过代理，BookingAspect 无感
            assertEquals("recorded-via-this", proxy.bookByThis());
            assertTrue(TransferTicketService.BOOKINGS.isEmpty(), "this 自调用不应被拦截");
            System.out.println("this 自调用: 切面拦截次数=" + TransferTicketService.BOOKINGS.size());

            // AopContext.currentProxy() 取回代理再调：走拦截器链，增强生效
            assertEquals("recorded-via-proxy", proxy.bookByProxy());
            assertEquals(1, TransferTicketService.BOOKINGS.size(), "currentProxy() 调用应被拦截");
            System.out.println("currentProxy() 调用: 切面拦截次数=" + TransferTicketService.BOOKINGS.size());
        }
    }

    @Test
    public void declareParentsIntroduction() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(IntroductionConfig.class)) {
            ContractPaperService contract = ctx.getBean(ContractPaperService.class);

            // 目标类没实现 Sealable，@DeclareParents 让代理"混入"了这个接口
            Sealable sealable = (Sealable) contract;
            assertFalse(sealable.isSealed());
            contract.append("第一条：交付日期");
            assertEquals(1, contract.getClauses().size());

            // 密封后写入：切面经 this() 绑定代理（实现了 Sealable），直接拒绝
            sealable.seal();
            assertTrue(sealable.isSealed());
            assertThrows(IllegalStateException.class, () -> contract.append("第二条：违约责任"));
            assertEquals(1, contract.getClauses().size(), "密封后写入应被拒绝");

            // 解除密封恢复写入——引介状态由 SealMixin 实例持有，可逆
            sealable.unseal();
            contract.append("第三条：争议解决");
            assertEquals(2, contract.getClauses().size());
            System.out.println("引介增强: 代理可转型为 Sealable，密封状态由 SealMixin 持有，写入被切面守卫");
        }
    }

    @Test
    public void advisedRuntimeAddAndRemoveAdvice() {
        List<String> dynamicLog = new CopyOnWriteArrayList<>();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopShowcaseConfig.class)) {
            TradeDeskService desk = ctx.getBean(TradeDeskService.class);

            // 所有 Spring AOP 代理都实现 Advised：可以窥视并修改代理内部的拦截器链
            Advised advised = (Advised) desk;
            int advisorCount = advised.getAdvisors().length;
            assertTrue(advisorCount > 0, "切点动物园的切面应已织入");
            System.out.println("代理内通知器数量: " + advisorCount
                    + "，目标类: " + advised.getTargetClass().getSimpleName());

            // 运行时动态追加通知（无需重建代理），settle 内部又跨 Bean 调到 PaymentChannel 的代理
            MethodBeforeAdvice dynamicAdvice = (method, args, target) ->
                    dynamicLog.add("dynamic-before:" + method.getName());
            advised.addAdvice(dynamicAdvice);
            desk.settle("SO-9301");
            assertEquals(List.of("dynamic-before:settle"), dynamicLog);
            System.out.println("运行时追加通知命中: " + dynamicLog.get(0));

            // 移除后立刻失效
            advised.removeAdvice(dynamicAdvice);
            desk.settle("SO-9302");
            assertEquals(1, dynamicLog.size(), "移除后不应再被动态通知拦截");
            System.out.println("移除动态通知后: settle 不再被拦截");
        }
    }

    @Test
    public void multiAspectOnionOrder() {
        PipelineService.FLOW.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(OnionConfig.class)) {
            assertEquals("pumped", ctx.getBean(PipelineService.class).pump());
            // @Order(5) 在外层、@Order(50) 在内层：先进入的后退出（洋葱模型）
            assertEquals(List.of(
                    "outer(5):enter",
                    "inner(50):enter",
                    "pump-target",
                    "inner(50):exit",
                    "outer(5):exit"), PipelineService.FLOW);
            System.out.println("多切面洋葱模型: " + PipelineService.FLOW);
        }
    }
}
