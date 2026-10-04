package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.scope.CartService;
import com.alec.InnovateX.spring.scope.LookupBoss;
import com.alec.InnovateX.spring.scope.ProviderBoss;
import com.alec.InnovateX.spring.scope.PrototypeProduct;
import com.alec.InnovateX.spring.scope.ScopeLog;
import com.alec.InnovateX.spring.scope.SessionCart;
import com.alec.InnovateX.spring.scope.SingletonBoss;
import com.alec.InnovateX.spring.scope.ThreadLocalResource;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bean 作用域（core/bean 第五课）：
 * - singleton 缓存复用 vs prototype 每次 getBean 新建（且容器不负责其销毁）
 * - 单例注入原型的「冻结」陷阱与三种解法：ObjectProvider / ObjectFactory / @Lookup 方法注入
 * - 自定义 Scope：SimpleThreadScope（线程隔离）
 * - scoped proxy：单例持有的是代理，方法调用时按当前上下文路由到对应作用域实例（Web 的 request/session 同理）
 */
public class ScopeTest {

    record Tag(String name) {
    }

    @Configuration
    static class SingletonConfig {
        @Bean
        Tag sharedTag() {
            return new Tag("唯一");
        }
    }

    /** singleton：默认作用域，容器级缓存，任何方式取到的都是同一实例。 */
    @Test
    public void singletonSameInstance() {
        try (var ctx = new AnnotationConfigApplicationContext(SingletonConfig.class)) {
            assertSame(ctx.getBean(Tag.class), ctx.getBean(Tag.class));
            assertSame(ctx.getBean("sharedTag"), ctx.getBean("sharedTag", Tag.class));
            assertTrue(ctx.isSingleton("sharedTag"));
            assertFalse(ctx.isPrototype("sharedTag"));
        }
    }

    @Configuration
    static class PrototypeConfig {
        @Bean
        @Scope("prototype")
        PrototypeProduct product() {
            return new PrototypeProduct();
        }
    }

    /** prototype：每次 getBean 都新建；容器关闭也不回调 @PreDestroy（创建后所有权归调用方）。 */
    @Test
    public void prototypeNewInstanceAndNoDestroy() {
        ScopeLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(PrototypeConfig.class)) {
            PrototypeProduct first = ctx.getBean(PrototypeProduct.class);
            PrototypeProduct second = ctx.getBean(PrototypeProduct.class);
            assertNotSame(first, second);
            assertTrue(ctx.isPrototype("product"));
        }
        assertEquals(2, ScopeLog.countOf("创建"), "两次 getBean 两个实例");
        assertEquals(0, ScopeLog.countOf("销毁"), "close 后原型 bean 的 @PreDestroy 没有被调用");
    }

    @Configuration
    static class BossConfig {
        @Bean
        @Scope("prototype")
        PrototypeProduct product() {
            return new PrototypeProduct();
        }

        @Bean
        SingletonBoss singletonBoss(PrototypeProduct product) {
            return new SingletonBoss(product);
        }
    }

    /** 冻结陷阱：单例只在创建那一刻注入一次原型，此后永远是同一个「过期」实例。 */
    @Test
    public void singletonHoldsFrozenPrototype() {
        try (var ctx = new AnnotationConfigApplicationContext(BossConfig.class)) {
            SingletonBoss boss = ctx.getBean(SingletonBoss.class);
            assertEquals(boss.heldProductId(), boss.heldProductId(),
                    "单例反复使用的是创建时注入的同一个原型实例");
            assertEquals(boss.heldProductId(), ctx.getBean(SingletonBoss.class).heldProductId());
        }
    }

    /** 解法一/二：ObjectProvider / ObjectFactory —— 注入查找句柄，每次 getObject() 都拿新原型。 */
    @Test
    public void providerAndFactorySolveFrozenPrototype() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean("product", PrototypeProduct.class, PrototypeProduct::new,
                    bd -> bd.setScope(BeanDefinition.SCOPE_PROTOTYPE));
            ctx.registerBean(ProviderBoss.class);
            ctx.refresh();

            ProviderBoss boss = ctx.getBean(ProviderBoss.class);
            assertNotEquals(boss.nextFromProvider(), boss.nextFromProvider(), "provider 每次都新实例");
            assertNotEquals(boss.nextFromFactory(), boss.nextFromFactory(), "factory 每次都新实例");
            assertNotEquals(boss.nextFromProvider(), boss.nextFromFactory());
        }
    }

    /** 解法三：@Lookup 方法注入 —— 容器覆写抽象方法，调用即 getBean（要求以「类」方式注册）。 */
    @Test
    public void lookupMethodInjection() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean("product", PrototypeProduct.class, PrototypeProduct::new,
                    bd -> bd.setScope(BeanDefinition.SCOPE_PROTOTYPE));
            ctx.registerBean(LookupBoss.class);
            ctx.refresh();

            LookupBoss boss = ctx.getBean(LookupBoss.class);
            assertNotEquals(boss.freshProductId(), boss.freshProductId(), "@Lookup 每次调用都取新实例");
            assertTrue(boss.getClass().getName().contains("CGLIB"), "容器生成了 LookupBoss 的子类");
        }
    }

    /** 自定义 Scope：SimpleThreadScope —— 同线程共享、跨线程隔离。 */
    @Test
    public void customThreadScope() throws Exception {
        var ctx = new AnnotationConfigApplicationContext();
        ctx.getBeanFactory().registerScope("thread", new SimpleThreadScope());
        ctx.registerBean("threadResource", ThreadLocalResource.class, ThreadLocalResource::new,
                bd -> bd.setScope("thread"));
        ctx.refresh();
        try {
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                CountDownLatch bothRunning = new CountDownLatch(2);
                Callable<List<Integer>> task = () -> {
                    bothRunning.countDown();
                    bothRunning.await();
                    ThreadLocalResource first = ctx.getBean("threadResource", ThreadLocalResource.class);
                    ThreadLocalResource second = ctx.getBean("threadResource", ThreadLocalResource.class);
                    return List.of(first.id(), second.id());
                };
                Future<List<Integer>> f1 = pool.submit(task);
                Future<List<Integer>> f2 = pool.submit(task);
                List<Integer> ids1 = f1.get(5, TimeUnit.SECONDS);
                List<Integer> ids2 = f2.get(5, TimeUnit.SECONDS);

                assertEquals(ids1.get(0), ids1.get(1), "同一线程内两次 getBean 是同一实例");
                assertEquals(ids2.get(0), ids2.get(1));
                assertNotEquals(ids1.get(0), ids2.get(0), "两个线程各自持有独立实例");
            } finally {
                pool.shutdownNow();
            }
        } finally {
            ctx.close();
        }
    }

    @Configuration
    static class ProxyConfig {
        @Bean
        @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
        SessionCart sessionCart() {
            return new SessionCart();
        }

        @Bean
        CartService cartService(SessionCart cart) {
            return new CartService(cart);
        }
    }

    /** scoped proxy：单例注入的是 CGLIB 代理，方法调用时按当前线程路由到 thread 作用域实例。 */
    @Test
    public void scopedProxyRoutesPerThread() throws Exception {
        var ctx = new AnnotationConfigApplicationContext();
        ctx.getBeanFactory().registerScope("thread", new SimpleThreadScope());
        ctx.register(ProxyConfig.class);
        ctx.refresh();
        try {
            Object injected = ctx.getBean("sessionCart");
            assertTrue(AopUtils.isCglibProxy(injected), "注入的是 CGLIB 代理而非原始对象");
            assertNotSame(injected.getClass(), SessionCart.class);

            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                CountDownLatch bothRunning = new CountDownLatch(2);
                Callable<Integer> task = () -> {
                    bothRunning.countDown();
                    bothRunning.await();
                    CartService service = ctx.getBean(CartService.class);
                    service.add("商品A");
                    service.add("商品B");
                    return service.size();
                };
                Future<Integer> f1 = pool.submit(task);
                Future<Integer> f2 = pool.submit(task);
                assertEquals(2, f1.get(5, TimeUnit.SECONDS), "线程1 的购物车只有自己的两件");
                assertEquals(2, f2.get(5, TimeUnit.SECONDS), "线程2 的购物车与线程1 互不干扰");
            } finally {
                pool.shutdownNow();
            }
        } finally {
            ctx.close();
        }
    }
}
