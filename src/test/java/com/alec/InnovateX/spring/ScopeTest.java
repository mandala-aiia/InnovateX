package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.scope.AopCircleB;
import com.alec.InnovateX.spring.scope.AopCircleConfig;
import com.alec.InnovateX.spring.scope.ConstructorCircleConfig;
import com.alec.InnovateX.spring.scope.EarlyReferenceProcessor;
import com.alec.InnovateX.spring.scope.LazyCircleConfig;
import com.alec.InnovateX.spring.scope.LazyPointCircleB;
import com.alec.InnovateX.spring.scope.PrototypeBean;
import com.alec.InnovateX.spring.scope.PrototypeCircleConfig;
import com.alec.InnovateX.spring.scope.ScopeConfig;
import com.alec.InnovateX.spring.scope.ScopeProxyHolder;
import com.alec.InnovateX.spring.scope.SetterCircleA;
import com.alec.InnovateX.spring.scope.SetterCircleB;
import com.alec.InnovateX.spring.scope.SetterCircleConfig;
import com.alec.InnovateX.spring.scope.SingletonBean;
import com.alec.InnovateX.spring.scope.ThreadScopeConfig;
import com.alec.InnovateX.spring.scope.ThreadScopedBean;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题③作用域与循环依赖：singleton/prototype、作用域代理、自定义作用域、
 * 三种循环依赖失败示例、@Lazy 打破循环、三级缓存早期引用
 */
public class ScopeTest {

    @Test
    public void singletonVsPrototype() {
        SingletonBean.instanceCount = 0;
        PrototypeBean.instanceCount = 0;
        PrototypeBean.destroyCount = 0;
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScopeConfig.class);
        try {
            // singleton：两次 getBean 同一实例，整个生命周期只创建一次
            assertSame(ctx.getBean(SingletonBean.class), ctx.getBean(SingletonBean.class));
            assertEquals(1, SingletonBean.instanceCount);
            // prototype：两次 getBean 两个新实例
            Object first = ctx.getBean(PrototypeBean.class);
            Object second = ctx.getBean(PrototypeBean.class);
            assertNotEquals(System.identityHashCode(first), System.identityHashCode(second));
            assertEquals(2, PrototypeBean.instanceCount);
            System.out.println("singleton 创建 " + SingletonBean.instanceCount + " 次，prototype getBean 两次创建了 "
                    + PrototypeBean.instanceCount + " 个实例");
        } finally {
            ctx.close();
        }
        // 容器关闭：singleton 的销毁回调由容器负责（见 AnnotationTest），
        // prototype 的 @PreDestroy 不会被调用——交还调用方后容器不再管理其生命周期
        assertEquals(0, PrototypeBean.destroyCount);
        System.out.println("容器关闭后 prototype 的 @PreDestroy 调用次数: " + PrototypeBean.destroyCount);
    }

    @Test
    public void scopedProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            ScopeProxyHolder holder = ctx.getBean(ScopeProxyHolder.class);
            String first = holder.firstCall();
            String second = holder.secondCall();
            assertNotEquals(first, second);
            System.out.println("第一次调用: " + first);
            System.out.println("第二次调用: " + second);
            System.out.println("=> 单例持有的是代理，每次方法调用都取到新的 prototype 实例");
        }
    }

    @Test
    public void customThreadScope() throws Exception {
        ThreadScopedBean.instanceCount = 0;
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadScopeConfig.class)) {
            // 同一线程：两个"实例"其实是同一个对象
            String first = ctx.getBean(ThreadScopedBean.class).whoAmI();
            String second = ctx.getBean(ThreadScopedBean.class).whoAmI();
            assertEquals(first, second);
            // 跨线程：各自创建自己的实例
            ExecutorService pool = Executors.newSingleThreadExecutor();
            try {
                Future<String> otherThread = pool.submit((Callable<String>) () ->
                        ctx.getBean(ThreadScopedBean.class).whoAmI());
                String fromOther = otherThread.get();
                assertNotEquals(first, fromOther);
                System.out.println("主线程: " + first);
                System.out.println("其他线程: " + fromOther);
                assertEquals(2, ThreadScopedBean.instanceCount);
            } finally {
                pool.shutdown();
            }
        }
    }

    @Test
    public void setterCircleClosedByDefault() {
        // setter/字段注入循环依赖：三级缓存默认开启，A<->B 闭环成立
        // 也可用 context.getDefaultListableBeanFactory().setAllowCircularReferences(false) 观察失败
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SetterCircleConfig.class)) {
            SetterCircleA a = ctx.getBean(SetterCircleA.class);
            assertEquals(a, a.getB().getA());
            System.out.println("setter 循环依赖: A->B->A 引用闭环成立");
        }
    }

    @Test
    public void constructorCircularFails() {
        // 构造器循环依赖：refresh 阶段抛异常（BeanCreationException 包着 BeanCurrentlyInCreationException）
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            ctx.register(ConstructorCircleConfig.class);
            BeansException e = assertThrows(BeansException.class, ctx::refresh);
            System.out.println("构造器循环依赖异常: " + e.getClass().getSimpleName() + " — " + rootMessage(e));
            assertTrue(rootMessage(e).contains("currently in creation")
                    || e.getMessage().contains("currently in creation"));
        } finally {
            ctx.close();
        }
    }

    @Test
    public void prototypeCircularFails() {
        // prototype 循环依赖：getBean 时抛 UnsatisfiedDependencyException（BeanCreationException 子类），
        // 其根因正是"Requested bean is currently in creation"——三级缓存只救 singleton
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(PrototypeCircleConfig.class)) {
            BeansException e = assertThrows(BeansException.class, () -> ctx.getBean("prototypeCircleA"));
            System.out.println("prototype 循环依赖异常: " + e.getClass().getSimpleName());
            assertTrue(rootMessage(e).contains("currently in creation"), "根因应为 BeanCurrentlyInCreationException: " + rootMessage(e));
        }
    }

    @Test
    public void setterCircularDisallowedFails() {
        // setter 循环依赖默认可行（三级缓存），关闭 allowCircularReferences 后同样失败
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            ctx.getDefaultListableBeanFactory().setAllowCircularReferences(false);
            ctx.register(SetterCircleConfig.class);
            BeansException e = assertThrows(BeansException.class, ctx::refresh);
            System.out.println("禁用循环依赖后的 setter 注入异常: " + e.getClass().getSimpleName() + " — " + rootMessage(e));
        } finally {
            ctx.close();
        }
        // 开关保持默认时可以成功创建
        try (AnnotationConfigApplicationContext ok = new AnnotationConfigApplicationContext(SetterCircleConfig.class)) {
            assertTrue(ok.getBean(com.alec.InnovateX.spring.scope.SetterCircleA.class).getB() != null);
            System.out.println("默认开启三级缓存时 setter 循环依赖创建成功");
        }
    }

    @Test
    public void lazyBreaksConstructorCircle() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LazyCircleConfig.class)) {
            LazyPointCircleB b = ctx.getBean(LazyPointCircleB.class);
            // 注入的是懒代理：类名带 $$SpringCGLIB$$（或 $$EnhancerBySpringCGLIB$$）
            String lazyAClass = b.getLazyA().getClass().getName();
            System.out.println("B 持有的 A 类型: " + lazyAClass);
            assertTrue(AopUtils.isAopProxy(b.getLazyA()), "注入点 @Lazy 应生成代理");
            // 第一次调用时才解析真实 Bean
            String result = b.callA();
            System.out.println("通过懒代理调用: " + result);
            assertTrue(result.startsWith("LazyPointCircleA.hello"));
        }
    }

    @Test
    public void threeLevelCacheWithAop() {
        EarlyReferenceProcessor.EARLY_REFERENCES.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopCircleConfig.class)) {
            // A 的早期引用确实走过三级缓存
            assertTrue(EarlyReferenceProcessor.EARLY_REFERENCES.contains("aopCircleA"));
            System.out.println("走过三级缓存早期曝光的 Bean: " + EarlyReferenceProcessor.EARLY_REFERENCES);
            // B 注入的 A 是提前生成的 CGLIB 代理
            AopCircleB b = ctx.getBean(AopCircleB.class);
            assertTrue(AopUtils.isAopProxy(b.getA()), "B 注入的应是 A 的早期代理");
            System.out.println("B 注入的 A 实际类型: " + b.getA().getClass().getName());
            // 调用代理方法触发切面
            String result = b.getA().hello();
            assertTrue(result.startsWith("AopCircleA.hello"));
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return String.valueOf(t.getMessage());
    }
}
