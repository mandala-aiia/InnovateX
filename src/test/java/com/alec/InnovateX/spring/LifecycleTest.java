package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.lifecycle.AlphaBean;
import com.alec.InnovateX.spring.lifecycle.BetaBean;
import com.alec.InnovateX.spring.lifecycle.DefinitionTweakingBFPP;
import com.alec.InnovateX.spring.lifecycle.FullLifecycleBean;
import com.alec.InnovateX.spring.lifecycle.GreeterService;
import com.alec.InnovateX.spring.lifecycle.LazyHeavyBean;
import com.alec.InnovateX.spring.lifecycle.LegacyCalendarBean;
import com.alec.InnovateX.spring.lifecycle.LifecycleLog;
import com.alec.InnovateX.spring.lifecycle.MetricsLifecycle;
import com.alec.InnovateX.spring.lifecycle.PlainGreeterService;
import com.alec.InnovateX.spring.lifecycle.PrototypeResourceBean;
import com.alec.InnovateX.spring.lifecycle.RecordingBeanPostProcessor;
import com.alec.InnovateX.spring.lifecycle.RecordingDestructionBPP;
import com.alec.InnovateX.spring.lifecycle.SchedulerLifecycle;
import com.alec.InnovateX.spring.lifecycle.WrappingBeanPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bean 与容器生命周期（core/bean 第四课）：
 * - 初始化全链路：构造 → Aware → BPP前置 → @PostConstruct → afterPropertiesSet → BPP后置
 * - 销毁链路：DABPP销毁前 → @PreDestroy → DisposableBean.destroy → destroyMethod
 * - initMethod/destroyMethod 属性、BFPP 先于一切实例化执行并改定义
 * - BeanPostProcessor 可替换 bean（AOP 的挂载点）、prototype 不回调销毁
 * - @Lazy 推迟创建、@DependsOn 控制初始化/销毁次序、SmartLifecycle 容器级启停（phase 顺序）
 */
public class LifecycleTest {

    @Configuration
    static class CoreConfig {
        @Bean
        static RecordingBeanPostProcessor recordingBeanPostProcessor() {
            return new RecordingBeanPostProcessor();
        }

        @Bean
        static RecordingDestructionBPP recordingDestructionBPP() {
            return new RecordingDestructionBPP();
        }

        @Bean
        FullLifecycleBean lifeCore() {
            return new FullLifecycleBean();
        }
    }

    /** 单个 bean 的初始化全链路（含 Aware 与两类 BPP 的相对位置），关闭后销毁链路同样断言。 */
    @Test
    public void fullInitAndDestroyChain() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(CoreConfig.class)) {
            assertEquals(List.of(
                    "构造",
                    "Aware:beanName=lifeCore",
                    "BPP前置:lifeCore",
                    "@PostConstruct",
                    "InitializingBean.afterPropertiesSet",
                    "BPP后置:lifeCore"), LifecycleLog.events(), "refresh 阶段的完整初始化时间线");
        }
        assertEquals(List.of(
                "构造",
                "Aware:beanName=lifeCore",
                "BPP前置:lifeCore",
                "@PostConstruct",
                "InitializingBean.afterPropertiesSet",
                "BPP后置:lifeCore",
                "DABPP销毁前:lifeCore",
                "@PreDestroy",
                "DisposableBean.destroy"), LifecycleLog.events(), "close 之后追加销毁时间线");
    }

    @Configuration
    static class LegacyConfig {
        /** BFPP/@Bean 方法建议声明 static：不依赖配置类实例即可提前执行，也避免过早初始化配置类。 */
        @Bean
        static DefinitionTweakingBFPP definitionTweakingBFPP() {
            return new DefinitionTweakingBFPP();
        }

        @Bean
        static RecordingBeanPostProcessor recordingBeanPostProcessor() {
            return new RecordingBeanPostProcessor();
        }

        @Bean
        static RecordingDestructionBPP recordingDestructionBPP() {
            return new RecordingDestructionBPP();
        }

        @Bean(initMethod = "startWork", destroyMethod = "offWork")
        LegacyCalendarBean lifeLegacy() {
            return new LegacyCalendarBean();
        }
    }

    /** initMethod/destroyMethod + BFPP：BFPP 在一切实例化之前改定义；属性注入夹在构造与初始化回调之间。 */
    @Test
    public void initMethodDestroyMethodAndBfppTiming() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(LegacyConfig.class)) {
            assertEquals(List.of(
                    "BFPP执行",
                    "构造:lifeLegacy",
                    "属性注入:owner=BFPP注入的主人",
                    "BPP前置:lifeLegacy",
                    "initMethod:startWork(owner=BFPP注入的主人)",
                    "BPP后置:lifeLegacy"), LifecycleLog.events());
        }
        assertEquals(List.of(
                "BFPP执行",
                "构造:lifeLegacy",
                "属性注入:owner=BFPP注入的主人",
                "BPP前置:lifeLegacy",
                "initMethod:startWork(owner=BFPP注入的主人)",
                "BPP后置:lifeLegacy",
                "DABPP销毁前:lifeLegacy",
                "destroyMethod:offWork"), LifecycleLog.events());
    }

    @Configuration
    static class WrapConfig {
        @Bean
        static WrappingBeanPostProcessor wrappingBeanPostProcessor() {
            return new WrappingBeanPostProcessor();
        }

        @Bean
        GreeterService lifeWrapped() {
            return new PlainGreeterService();
        }
    }

    /** BeanPostProcessor 可以「偷天换日」：初始化后返回代理替换原对象，调用方拿到的已是代理。 */
    @Test
    public void bppCanReplaceBeanWithProxy() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(WrapConfig.class)) {
            GreeterService svc = ctx.getBean(GreeterService.class);
            assertEquals("原始问候[被BPP代理]", svc.hello());
            assertTrue(Proxy.isProxyClass(svc.getClass()), "拿到的是 JDK 动态代理");
            assertTrue(LifecycleLog.events().contains("BPP替换:lifeWrapped"));
        }
    }

    @Configuration
    static class LazyConfig {
        @Bean
        @Lazy
        LazyHeavyBean lazyHeavy() {
            return new LazyHeavyBean();
        }

        @Bean
        @Lazy
        GreeterService lazyEagerTwin() {
            return new PlainGreeterService();
        }
    }

    /** @Lazy：refresh 不创建、首次 getBean 才创建；同一个容器里可以按 bean 粒度混用懒/急切。 */
    @Test
    public void lazyInitializationDefersCreation() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(LazyConfig.class)) {
            assertEquals(List.of(), LifecycleLog.events(), "refresh 完成后 @Lazy bean 尚未构造");
            ctx.getBean(LazyHeavyBean.class);
            assertEquals(List.of("构造:lazyHeavy"), LifecycleLog.events(), "首次 getBean 触发构造");
        }
    }

    @Configuration
    static class DependsOnConfig {
        /** beta 声明在前，但 @DependsOn 要求 alpha 先就绪。 */
        @Bean
        @DependsOn("alphaBean")
        BetaBean betaBean() {
            return new BetaBean();
        }

        @Bean
        AlphaBean alphaBean() {
            return new AlphaBean();
        }
    }

    /** @DependsOn：没有直接依赖关系时强制初始化顺序，销毁顺序自动取反。 */
    @Test
    public void dependsOnOrdersInitAndDestroy() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(DependsOnConfig.class)) {
            assertEquals(List.of("构造:alpha", "构造:beta"), LifecycleLog.events(),
                    "alpha 先于 beta 初始化");
        }
        assertEquals(List.of("构造:alpha", "构造:beta", "销毁:beta", "销毁:alpha"), LifecycleLog.events(),
                "销毁顺序与初始化相反");
    }

    @Configuration
    static class PrototypeConfig {
        @Bean
        @Scope("prototype")
        PrototypeResourceBean lifePrototype() {
            return new PrototypeResourceBean();
        }
    }

    /** prototype：每次 getBean 新实例；容器关闭时不执行任何销毁回调（谁创建谁负责）。 */
    @Test
    public void prototypeBeansAreNotDestroyedByContainer() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(PrototypeConfig.class)) {
            PrototypeResourceBean first = ctx.getBean(PrototypeResourceBean.class);
            PrototypeResourceBean second = ctx.getBean(PrototypeResourceBean.class);
            assertNotSame(first, second);
        }
        // close 之后：只有两次构造记录，没有销毁记录
        assertEquals(2, LifecycleLog.events().size());
        assertEquals(2, LifecycleLog.events().stream().filter(e -> e.startsWith("构造")).count());
        assertFalse(LifecycleLog.events().stream().anyMatch(e -> e.startsWith("销毁")),
                "prototype 的 @PreDestroy 不会被容器调用");
    }

    @Configuration
    static class SmartConfig {
        @Bean
        SchedulerLifecycle schedulerLifecycle() {
            return new SchedulerLifecycle();
        }

        @Bean
        MetricsLifecycle metricsLifecycle() {
            return new MetricsLifecycle();
        }
    }

    /** SmartLifecycle（容器级生命周期）：phase 小的先 start、后 stop；refresh 末尾启动、close 最先停止。 */
    @Test
    public void smartLifecyclePhaseOrdering() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(SmartConfig.class)) {
            assertTrue(ctx.getBean(MetricsLifecycle.class).isRunning(), "autoStartup 默认开启");
            assertEquals(List.of("SL.start[phase=0:metrics]", "SL.start[phase=100:scheduler]"),
                    LifecycleLog.events(), "phase 升序启动");
        }
        assertEquals(List.of(
                "SL.start[phase=0:metrics]",
                "SL.start[phase=100:scheduler]",
                "SL.stop[phase=100:scheduler]",
                "SL.stop[phase=0:metrics]"), LifecycleLog.events(), "stop 与 start 完全反序");
    }

    @Configuration
    static class GrandConfig {
        @Bean
        static DefinitionTweakingBFPP bfpp() {
            return new DefinitionTweakingBFPP();
        }

        @Bean
        static RecordingBeanPostProcessor recordingBeanPostProcessor() {
            return new RecordingBeanPostProcessor();
        }

        @Bean
        static RecordingDestructionBPP recordingDestructionBPP() {
            return new RecordingDestructionBPP();
        }

        @Bean(initMethod = "startWork", destroyMethod = "offWork")
        LegacyCalendarBean lifeLegacy() {
            return new LegacyCalendarBean();
        }

        @Bean
        FullLifecycleBean lifeCore() {
            return new FullLifecycleBean();
        }

        @Bean
        @DependsOn("alphaBean")
        BetaBean betaBean() {
            return new BetaBean();
        }

        @Bean
        AlphaBean alphaBean() {
            return new AlphaBean();
        }

        @Bean
        MetricsLifecycle metricsLifecycle() {
            return new MetricsLifecycle();
        }

        @Bean
        SchedulerLifecycle schedulerLifecycle() {
            return new SchedulerLifecycle();
        }
    }

    /** 综合时间线：一个容器从 BFPP → 逐个实例化 → SmartLifecycle 启动，到 close 时反序收尾的全过程。 */
    @Test
    public void grandTimeline() {
        LifecycleLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext(GrandConfig.class)) {
            assertEquals(List.of(
                    "BFPP执行",
                    "构造:lifeLegacy",
                    "属性注入:owner=BFPP注入的主人",
                    "BPP前置:lifeLegacy",
                    "initMethod:startWork(owner=BFPP注入的主人)",
                    "BPP后置:lifeLegacy",
                    "构造",
                    "Aware:beanName=lifeCore",
                    "BPP前置:lifeCore",
                    "@PostConstruct",
                    "InitializingBean.afterPropertiesSet",
                    "BPP后置:lifeCore",
                    "构造:alpha",
                    "构造:beta",
                    "SL.start[phase=0:metrics]",
                    "SL.start[phase=100:scheduler]"), LifecycleLog.events(), "refresh 全程");
        }
        assertEquals(List.of(
                "BFPP执行",
                "构造:lifeLegacy",
                "属性注入:owner=BFPP注入的主人",
                "BPP前置:lifeLegacy",
                "initMethod:startWork(owner=BFPP注入的主人)",
                "BPP后置:lifeLegacy",
                "构造",
                "Aware:beanName=lifeCore",
                "BPP前置:lifeCore",
                "@PostConstruct",
                "InitializingBean.afterPropertiesSet",
                "BPP后置:lifeCore",
                "构造:alpha",
                "构造:beta",
                "SL.start[phase=0:metrics]",
                "SL.start[phase=100:scheduler]",
                "SL.stop[phase=100:scheduler]",
                "SL.stop[phase=0:metrics]",
                "销毁:beta",
                "销毁:alpha",
                "DABPP销毁前:lifeCore",
                "@PreDestroy",
                "DisposableBean.destroy",
                "DABPP销毁前:lifeLegacy",
                "destroyMethod:offWork"), LifecycleLog.events(), "close 反序收尾");
    }
}
