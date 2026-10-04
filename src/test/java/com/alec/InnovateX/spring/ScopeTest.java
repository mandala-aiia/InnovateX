package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.scope.AlarmCenter;
import com.alec.InnovateX.spring.scope.ConstructorCircleConfig;
import com.alec.InnovateX.spring.scope.Console;
import com.alec.InnovateX.spring.scope.EarlyProxyConfig;
import com.alec.InnovateX.spring.scope.EarlyProxyProcessor;
import com.alec.InnovateX.spring.scope.Engine;
import com.alec.InnovateX.spring.scope.FrontDesk;
import com.alec.InnovateX.spring.scope.LazyCircleConfig;
import com.alec.InnovateX.spring.scope.MenuService;
import com.alec.InnovateX.spring.scope.MonitorStation;
import com.alec.InnovateX.spring.scope.PrototypeCircleConfig;
import com.alec.InnovateX.spring.scope.ReceiptPrinter;
import com.alec.InnovateX.spring.scope.ScopeBasicsConfig;
import com.alec.InnovateX.spring.scope.SetterCircleConfig;
import com.alec.InnovateX.spring.scope.ThreadScope;
import com.alec.InnovateX.spring.scope.ThreadScopeConfig;
import com.alec.InnovateX.spring.scope.Ticket;
import com.alec.InnovateX.spring.scope.TicketMachine;
import com.alec.InnovateX.spring.scope.TraceTag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题：作用域（singleton/prototype/自定义 Thread 作用域/作用域代理）
 * 与循环依赖（setter 可解、构造器无解、prototype 无解、@Lazy 打破、三级缓存+AOP 早期代理）。
 * <p>
 * 约定：try-with-resources 保证容器关闭；多线程断言一律用 ExecutorService + Future，
 * 不做 sleep 裸等待。
 */
public class ScopeTest {

    @Test
    public void singletonVsPrototype() {
        TicketMachine.CREATED.set(0);
        TicketMachine.DESTROYED.set(0);
        Ticket.CREATED.set(0);
        Ticket.DESTROYED.set(0);

        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScopeBasicsConfig.class)) {
            // singleton：两次 getBean 同一个对象，refresh 阶段就已预实例化（CREATED==1）
            TicketMachine first = ctx.getBean(TicketMachine.class);
            TicketMachine second = ctx.getBean(TicketMachine.class);
            assertSame(first, second, "singleton 必须同引用");
            assertEquals(1, TicketMachine.CREATED.get());

            // prototype：两次 getBean 两个新实例；依赖的单例取号机状态被两个实例共享
            Ticket ticketA = ctx.getBean(Ticket.class);
            Ticket ticketB = ctx.getBean(Ticket.class);
            assertNotEquals(System.identityHashCode(ticketA), System.identityHashCode(ticketB), "prototype 必须新实例");
            assertEquals(2, Ticket.CREATED.get());
            assertEquals(1, ticketA.getNumber());
            assertEquals(2, ticketB.getNumber(), "单例取号机的递增状态被两个 prototype 共享");
            System.out.println("[测试] singleton 创建 " + TicketMachine.CREATED.get() + " 个；"
                    + "prototype 两次 getBean 创建 " + Ticket.CREATED.get() + " 个");
        }
        // 容器关闭后：singleton 的 @PreDestroy 被触发，prototype 的纹丝不动——生命周期归调用方
        assertEquals(1, TicketMachine.DESTROYED.get(), "容器负责 singleton 的销毁");
        assertEquals(0, Ticket.DESTROYED.get(), "容器不管 prototype 的销毁");
        System.out.println("[测试] 容器关闭后：singleton 销毁 " + TicketMachine.DESTROYED.get()
                + " 次，prototype 销毁 " + Ticket.DESTROYED.get() + " 次");
    }

    @Test
    public void scopedProxyDeliversFreshPrototype() {
        ReceiptPrinter.CREATED.set(0);
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScopeBasicsConfig.class)) {
            FrontDesk desk = ctx.getBean(FrontDesk.class);
            // 注入到单例里的是 CGLIB 作用域代理，不是 ReceiptPrinter 本尊
            assertTrue(AopUtils.isAopProxy(desk.getReceiptPrinter()), "注入的应是作用域代理");
            System.out.println("[测试] FrontDesk 持有的类型: " + desk.getReceiptPrinter().getClass().getName());

            // 代理语义：单例只注入一次，但每次方法调用都"现取"一个新的 prototype 实例
            String first = desk.printFirst();
            String second = desk.printSecond();
            assertNotEquals(first, second, "两次调用应落在不同的 prototype 实例上");
            assertEquals(2, ReceiptPrinter.CREATED.get(), "两次调用应创建两个目标实例");
            System.out.println("[测试] 第一次: " + first);
            System.out.println("[测试] 第二次: " + second);
        }
    }

    @Test
    public void customThreadScope() throws Exception {
        TraceTag.CREATED.set(0);
        TraceTag.DESTROYED.set(0);
        ThreadScope.EVENTS.clear();

        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadScopeConfig.class)) {
            // 同一线程：两次 getBean 命中线程本地缓存 → 同一实例
            TraceTag mainFirst = ctx.getBean(TraceTag.class);
            TraceTag mainSecond = ctx.getBean(TraceTag.class);
            assertSame(mainFirst, mainSecond, "同线程应共享实例");
            assertEquals(1, TraceTag.CREATED.get());

            // 跨线程：线程本地表互相隔离 → 各自创建
            ExecutorService pool = Executors.newSingleThreadExecutor();
            try {
                Future<TraceTag> worker = pool.submit((Callable<TraceTag>) () -> ctx.getBean(TraceTag.class));
                TraceTag otherThread = worker.get(); // Future.get 完成即结果就绪，无需 sleep
                assertNotEquals(System.identityHashCode(mainFirst), System.identityHashCode(otherThread),
                        "跨线程应各自实例");
                assertEquals(2, TraceTag.CREATED.get(), "两个线程恰好两个实例");
                System.out.println("[测试] 主线程: " + mainFirst.describe());
                System.out.println("[测试] 工作线程: " + otherThread.describe());
            } finally {
                pool.shutdown();
            }

            // Scope 接口契约观察：容器为 thread 作用域 Bean 注册过销毁回调
            assertTrue(ThreadScope.EVENTS.containsKey("traceTag:callbackRegistered"),
                    "容器应通过 registerDestructionCallback 登记销毁逻辑");
            // destroyScopedBean：走 scope.remove + 完整销毁路径（含 @PreDestroy）
            ctx.getBeanFactory().destroyScopedBean("traceTag");
            assertTrue(ThreadScope.EVENTS.containsKey("traceTag:removed"), "remove 应被调用");
            assertEquals(1, TraceTag.DESTROYED.get(), "销毁路径应触发 @PreDestroy");
            // 移除后再取：作用域重新向容器要一个实例（ThreadScope.get 的缓存未命中分支）
            ctx.getBean(TraceTag.class);
            assertEquals(3, TraceTag.CREATED.get());
        }
    }

    @Test
    public void setterCircleResolvedByDefault() {
        // 字段/setter 注入循环依赖：三级缓存默认开启，A→B→A 引用闭环成立
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SetterCircleConfig.class)) {
            MenuService menu = ctx.getBean(MenuService.class);
            assertSame(menu, menu.getStockService().getMenuService(), "A->B->A 闭环");
            System.out.println("[测试] " + menu.signature() + " <-> "
                    + menu.getStockService().getClass().getSimpleName() + " 循环依赖被三级缓存解开");
        }
    }

    @Test
    public void setterCircleForbiddenByFlag() {
        // 手动关闭循环依赖开关：AbstractAutowireCapableBeanFactory#setAllowCircularReferences
        // 在 7.0.9 仍存在且未标过时（本课注释即基于 7.0.9 验证）；Spring Boot 2.6+ 走的正是这个开关
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            ctx.getDefaultListableBeanFactory().setAllowCircularReferences(false);
            ctx.register(SetterCircleConfig.class);
            BeansException e = assertThrows(BeansException.class, ctx::refresh);
            String root = rootMessage(e);
            System.out.println("[测试] 关闭开关后: " + e.getClass().getSimpleName() + "，根因: " + root);
            assertTrue(root.contains("currently in creation"),
                    "根因应为 BeanCurrentlyInCreationException: " + root);
        } finally {
            ctx.close();
        }
        // 对照组：开关保持默认（true）时同一个配置可以正常创建
        try (AnnotationConfigApplicationContext ok = new AnnotationConfigApplicationContext(SetterCircleConfig.class)) {
            assertFalse(ok.getBean(MenuService.class).getStockService() == null);
            System.out.println("[测试] 对照组：默认三级缓存开启时创建成功");
        }
    }

    @Test
    public void constructorCircleFailsAtRefresh() {
        // 构造器注入循环：refresh（预实例化单例）阶段即失败，无三级缓存可救
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            ctx.register(ConstructorCircleConfig.class);
            BeansException e = assertThrows(BeansException.class, ctx::refresh);
            String root = rootMessage(e);
            System.out.println("[测试] 构造器循环: " + e.getClass().getSimpleName() + "，根因: " + root);
            assertTrue(root.contains("currently in creation"), "根因应含 currently in creation: " + root);
        } finally {
            ctx.close();
        }
    }

    @Test
    public void prototypeCircleFailsAtGetBean() {
        // prototype 循环：refresh 成功（懒创建），第一次 getBean 时递归创建爆栈前被检测打断
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(PrototypeCircleConfig.class)) {
            BeansException e = assertThrows(BeansException.class, () -> ctx.getBean("jobShardA"));
            String root = rootMessage(e);
            System.out.println("[测试] prototype 循环: " + e.getClass().getSimpleName() + "，根因: " + root);
            assertTrue(root.contains("currently in creation"),
                    "三级缓存只救 singleton，prototype 根因也是 currently in creation: " + root);
        }
    }

    @Test
    public void lazyProxyBreaksConstructorCircle() {
        Console.CREATED.set(0);
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LazyCircleConfig.class)) {
            Engine engine = ctx.getBean(Engine.class);
            // refresh 已完成、但尚未有任何调用：真实 Console 一个都没创建（@Lazy Bean + 注入点双懒）
            assertEquals(0, Console.CREATED.get(), "首次调用前真实目标不应被创建");
            // 注入的是 CGLIB 懒代理，不是 Console 本尊
            Object held = engine.getConsole();
            assertTrue(AopUtils.isAopProxy(held), "注入点 @Lazy 应交付代理");
            assertFalse(held.getClass().equals(Console.class), "代理类应是 Console 的子类而非本尊");
            System.out.println("[测试] Engine 持有的类型: " + held.getClass().getName());

            // 首次调用：代理此刻才去容器解析真实 Console（环在调用时刻才闭合）
            String result = engine.renderViaConsole();
            assertTrue(result.startsWith("Console.render"));
            assertEquals(1, Console.CREATED.get(), "首次调用后才创建真实目标");
            System.out.println("[测试] 首次调用结果: " + result);
        }
    }

    @Test
    public void earlyReferenceExposesProxy() {
        EarlyProxyProcessor.EARLY_EXPOSED.clear();
        EarlyProxyProcessor.INTERCEPTED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EarlyProxyConfig.class)) {
            // 证据一：alarmCenter 确实走过三级缓存的早期曝光
            assertTrue(EarlyProxyProcessor.EARLY_EXPOSED.contains("alarmCenter"),
                    "alarmCenter 应被 getEarlyBeanReference 提前曝光");
            System.out.println("[测试] 走过早期曝光的 Bean: " + EarlyProxyProcessor.EARLY_EXPOSED);

            // 证据二：循环依赖方拿到的是提前生成的代理
            MonitorStation station = ctx.getBean(MonitorStation.class);
            Object injected = station.getAlarmCenter();
            assertTrue(AopUtils.isAopProxy(injected), "循环依赖方注入的应是早期代理");
            System.out.println("[测试] MonitorStation 持有的类型: " + injected.getClass().getName());

            // 证据三：注入的代理与容器最终暴露的 Bean 是同一个对象（同身份）
            Object finalBean = ctx.getBean("alarmCenter");
            assertSame(injected, finalBean, "早期代理应与最终代理同身份");
            assertEquals(1, AlarmCenter.CREATED.get(), "目标类只应实例化一次");

            // 证据四：代理方法可正常调用且被拦截（目标 + 环 + AOP 三者共存）
            String ring = ((AlarmCenter) injected).ring();
            assertTrue(ring.startsWith("AlarmCenter.ring"));
            assertTrue(EarlyProxyProcessor.INTERCEPTED.contains("ring"));
            System.out.println("[测试] 经代理调用: " + ring);
        }
    }

    /** 层层下钻取根因消息：BeanCreationException 的 cause 链末端才是知识点的真正异常 */
    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return String.valueOf(t.getMessage());
    }
}
