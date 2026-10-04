package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.aop.AppAnnotationAspect;
import com.alec.InnovateX.spring.aop.AopConfig;
import com.alec.InnovateX.spring.aop.AopMessageService;
import com.alec.InnovateX.spring.aop.AopOrderService;
import com.alec.InnovateX.spring.aop.AopOrderServiceImpl;
import com.alec.InnovateX.spring.aop.DocumentService;
import com.alec.InnovateX.spring.aop.FinalTargetService;
import com.alec.InnovateX.spring.aop.IntroAopConfig;
import com.alec.InnovateX.spring.aop.Lockable;
import com.alec.InnovateX.spring.aop.LogAspect;
import com.alec.InnovateX.spring.aop.MultiAspectConfig;
import com.alec.InnovateX.spring.aop.MultiAspectService;
import com.alec.InnovateX.spring.aop.PointcutZooAspect;
import com.alec.InnovateX.spring.aop.SelfInvokeService;
import com.alec.InnovateX.spring.aop.ZooTargetService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题④AOP 深度：@AspectJ 五种通知、JDK vs CGLIB、编程式 ProxyFactory、
 * 切点表达式进阶、自调用失效与 currentProxy 修复
 */
public class AopDeepTest {

    @Test
    public void aspectFiveAdvices() {
        LogAspect.LOG.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopConfig.class)) {
            AopOrderService service = ctx.getBean(AopOrderService.class);
            service.createOrder("SO-2001");
            // 正常返回的通知顺序：around前 -> before -> 目标方法 -> afterReturning -> after -> around后
            List<String> expected = List.of("around:before", "before:createOrder",
                    "afterReturning:订单已创建: SO-2001", "after:createOrder", "around:after");
            assertEquals(expected, LogAspect.LOG);
            System.out.println("正常返回的通知顺序: " + LogAspect.LOG);

            // 异常路径：@AfterThrowing 接住，@After 仍然执行（finally 语义）
            LogAspect.LOG.clear();
            assertThrows(IllegalArgumentException.class, () -> service.cancelOrder(" "));
            List<String> throwPath = List.of("around:before", "before:cancelOrder",
                    "afterThrowing:IllegalArgumentException", "after:cancelOrder", "around:after-throwing");
            assertEquals(throwPath, LogAspect.LOG);
            System.out.println("异常路径的通知顺序: " + LogAspect.LOG);
        }
    }

    @Test
    public void jdkVsCglib() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopConfig.class)) {
            // 有接口 -> JDK 动态代理（基于接口反射，com.sun.proxy.$Proxy...）
            AopOrderService orderService = ctx.getBean(AopOrderService.class);
            assertTrue(AopUtils.isJdkDynamicProxy(orderService));
            System.out.println("有接口的代理类型: " + orderService.getClass().getName());

            // 无接口 -> CGLIB 子类代理（类名带 $$SpringCGLIB$$）
            AopMessageService messageService = ctx.getBean(AopMessageService.class);
            assertTrue(AopUtils.isCglibProxy(messageService));
            assertFalse(messageService instanceof AopOrderService);
            System.out.println("无接口的代理类型: " + messageService.getClass().getName());
        }
    }

    @Test
    public void programmaticProxyFactory() throws Exception {
        // 1) 显式添加接口 -> JDK 代理
        ProxyFactory jdkFactory = new ProxyFactory(new AopOrderServiceImpl());
        jdkFactory.addInterface(AopOrderService.class);
        jdkFactory.addAdvice((MethodBeforeAdvice) (method, args, target) ->
                System.out.println("[ProxyFactory-JDK] 拦截: " + method.getName()));
        AopOrderService jdkProxy = (AopOrderService) jdkFactory.getProxy();
        assertTrue(AopUtils.isJdkDynamicProxy(jdkProxy));
        System.out.println("ProxyFactory JDK 代理: " + jdkProxy.createOrder("SO-3001"));

        // 2) 无接口 -> 自动走 CGLIB
        ProxyFactory cglibFactory = new ProxyFactory(new AopMessageService());
        cglibFactory.addAdvice((MethodBeforeAdvice) (method, args, target) ->
                System.out.println("[ProxyFactory-CGLIB] 拦截: " + method.getName()));
        AopMessageService cglibProxy = (AopMessageService) cglibFactory.getProxy();
        assertTrue(AopUtils.isCglibProxy(cglibProxy));
        System.out.println("ProxyFactory CGLIB 代理: " + cglibProxy.send("hello"));

        // 3) final 类无法被 CGLIB 继承 -> 创建代理时直接失败
        ProxyFactory finalFactory = new ProxyFactory(new FinalTargetService());
        assertThrows(Exception.class, finalFactory::getProxy);
        System.out.println("final 类强制 CGLIB 代理: 创建失败（Cannot subclass final class）");
    }

    @Test
    public void pointcutZoo() {
        PointcutZooAspect.MATCHED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopConfig.class)) {
            ZooTargetService zoo = ctx.getBean(ZooTargetService.class);
            zoo.withinDemo();
            assertTrue(PointcutZooAspect.MATCHED.contains("within:withinDemo"));
            zoo.targetDemo();
            assertTrue(PointcutZooAspect.MATCHED.contains("target:targetDemo"));
            zoo.argsDemo("apple", 3);
            assertTrue(PointcutZooAspect.MATCHED.contains("args:argsDemo"));
            zoo.beanDemo();
            assertTrue(PointcutZooAspect.MATCHED.contains("bean:beanDemo"));
            System.out.println("各切点表达式命中记录: " + PointcutZooAspect.MATCHED);
        }
    }

    @Test
    public void selfInvocation() {
        SelfInvokeService.LOG.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopConfig.class)) {
            SelfInvokeService proxy = ctx.getBean(SelfInvokeService.class);
            // this 调用绕过代理：切面失效
            String failResult = proxy.outerFail();
            assertEquals("inner called by outerFail", failResult);
            assertTrue(SelfInvokeService.LOG.isEmpty(), "this.inner() 不应被拦截");
            System.out.println("this 自调用: " + failResult + "，切面拦截次数=" + SelfInvokeService.LOG.size());

            // AopContext.currentProxy() 显式走代理：切面生效
            String fixedResult = proxy.outerFixed();
            assertEquals("inner called by outerFixed", fixedResult);
            assertEquals(1, SelfInvokeService.LOG.size());
            System.out.println("currentProxy() 调用: " + fixedResult + "，切面拦截次数=" + SelfInvokeService.LOG.size());
        }
    }

    @Test
    public void declareParentsIntroduction() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(IntroAopConfig.class)) {
            DocumentService service = ctx.getBean(DocumentService.class);
            // 目标类没有实现 Lockable，但 @DeclareParents 让代理"混入"了这个接口
            Lockable lockable = (Lockable) service;
            assertFalse(lockable.isLocked());
            service.write("第一段内容");
            assertEquals(1, service.getContents().size());

            // 锁定后再写入：切面通过 this() 绑定代理（实现了 Lockable），拒绝写入
            lockable.lock();
            assertTrue(lockable.isLocked());
            assertThrows(IllegalStateException.class, () -> service.write("第二段内容"));
            assertEquals(1, service.getContents().size());
            System.out.println("引介增强: 代理可转型为 Lockable，锁定状态由 LockMixin 持有，写入被切面拒绝");
        }
    }

    @Test
    public void advisedDynamicAdvice() {
        List<String> dynamicLog = new ArrayList<>();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(IntroAopConfig.class)) {
            DocumentService service = ctx.getBean(DocumentService.class);
            // 所有 Spring AOP 代理都实现 Advised：可以窥视并修改代理内部的拦截器链
            Advised advised = (Advised) service;
            System.out.println("代理内当前通知器数量: " + advised.getAdvisors().length
                    + "，目标类: " + advised.getTargetClass().getSimpleName());

            // 运行时动态追加通知（无需重新创建代理）
            MethodBeforeAdvice dynamicAdvice = (method, args, target) ->
                    dynamicLog.add("dynamic-before:" + method.getName());
            advised.addAdvice(dynamicAdvice);
            service.write("带动态通知");
            assertEquals(1, dynamicLog.size());
            assertEquals("dynamic-before:write", dynamicLog.get(0));
            System.out.println("运行时追加通知: " + dynamicLog.get(0));

            // 移除后立刻失效
            advised.removeAdvice(dynamicAdvice);
            service.write("移除后");
            assertEquals(1, dynamicLog.size());
            System.out.println("移除动态通知后: write 不再被拦截");
        }
    }

    @Test
    public void annotationPointcut() {
        // @annotation 切点：匹配"标注了 @AppAnnotation"的方法，注解本体直接绑定为通知参数
        AppAnnotationAspect.MATCHED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopConfig.class)) {
            ZooTargetService zoo = ctx.getBean(ZooTargetService.class);
            assertEquals("annotationDemo", zoo.annotationDemo());
            assertTrue(AppAnnotationAspect.MATCHED.contains("annotation:annotationDemo:zoo-annotation-demo"));
            System.out.println("@annotation 切点命中: " + AppAnnotationAspect.MATCHED);
        }
    }

    @Test
    public void multiAspectOrder() {
        MultiAspectService.EVENTS.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiAspectConfig.class)) {
            assertEquals("done", ctx.getBean(MultiAspectService.class).work());
            // @Order(1) 在外层、@Order(2) 在内层：洋葱模型（先进入的后退出）
            assertEquals(List.of(
                    "aspect1-around-before",
                    "aspect2-around-before",
                    "target-work",
                    "aspect2-around-after",
                    "aspect1-around-after"
            ), MultiAspectService.EVENTS);
            System.out.println("多切面洋葱模型: " + MultiAspectService.EVENTS);
        }
    }
}
