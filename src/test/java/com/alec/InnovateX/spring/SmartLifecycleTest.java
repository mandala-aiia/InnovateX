package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.lifecycle.AllSingletonsReadyHook;
import com.alec.InnovateX.spring.lifecycle.ComputeLifecycle;
import com.alec.InnovateX.spring.lifecycle.FullChainBean;
import com.alec.InnovateX.spring.lifecycle.FullChainConfig;
import com.alec.InnovateX.spring.lifecycle.LifecycleEventLog;
import com.alec.InnovateX.spring.lifecycle.PlainWorkerBean;
import com.alec.InnovateX.spring.lifecycle.SmartLifecycleConfig;
import com.alec.InnovateX.spring.lifecycle.StorageLifecycle;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题：生命周期。覆盖知识点：
 * 1. 满配 Bean 完整 13 步回调链的精确顺序（初始化 10 步 + 销毁反序 3 步）；
 * 2. SmartLifecycle phase 升序启动、降序停止、autoStartup 自动启动；
 * 3. SmartInitializingSingleton 全体非懒单例就绪后回调一次（对比 @PostConstruct 只代表单个 Bean 就绪）。
 */
public class SmartLifecycleTest {

    /** 初始化阶段 10 步（销毁前）。注意 06/07：Spring 7 实测自定义 BPP 前置先于 @PostConstruct，
     *  原因见 ChainWatchProcessor 类注释（内部注解处理器被挪到处理器链末尾） */
    private static final List<String> EXPECTED_INIT_CHAIN = List.of(
            "01-构造器实例化",
            "02-属性填充：@Autowired setter 注入",
            "03-BeanNameAware.setBeanName",
            "04-BeanFactoryAware.setBeanFactory",
            "05-ApplicationContextAware.setApplicationContext",
            "06-自定义BeanPostProcessor.postProcessBeforeInitialization",
            "07-@PostConstruct",
            "08-InitializingBean.afterPropertiesSet",
            "09-@Bean(initMethod=manualInit)",
            "10-自定义BeanPostProcessor.postProcessAfterInitialization");

    /** 销毁阶段 3 步（与初始化大体反序） */
    private static final List<String> EXPECTED_DESTROY_CHAIN = List.of(
            "11-@PreDestroy",
            "12-DisposableBean.destroy",
            "13-@Bean(destroyMethod=manualDestroy)");

    /** refresh 到 close 的完整启停剧本（SmartLifecycle 主题） */
    private static final List<String> EXPECTED_START_STOP_SCRIPT = List.of(
            "hook:@PostConstruct(worker已就绪=false)",
            "worker:@PostConstruct",
            "hook:afterSingletonsInstantiated(worker已就绪=true)",
            "storage:start(phase=10)",
            "compute:start(phase=20,storage运行中=true)",
            "compute:stop(storage仍在运行=true)",
            "storage:stop(phase=10)");

    @Test
    public void fullLifecycleThirteenSteps() {
        FullChainBean.TRACE.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(FullChainConfig.class)) {
            // 容器打开期间只有初始化 10 步：精确断言顺序
            assertEquals(EXPECTED_INIT_CHAIN, FullChainBean.TRACE, "初始化阶段应为固定 10 步");
            System.out.println("[SmartLifecycleTest] 初始化 10 步: " + FullChainBean.TRACE);
        }
        // try-with-resources 的 close() 触发销毁 3 步（此时容器已关闭，静态 TRACE 仍可读）
        List<String> full = List.copyOf(FullChainBean.TRACE);
        assertEquals(EXPECTED_INIT_CHAIN.size() + EXPECTED_DESTROY_CHAIN.size(), full.size(), "共 13 步");
        assertEquals(EXPECTED_DESTROY_CHAIN, full.subList(10, 13), "销毁阶段反序三步");
        System.out.println("[SmartLifecycleTest] 完整 13 步: " + full);
    }

    @Test
    public void smartLifecyclePhaseOrderAndAutoStartup() {
        LifecycleEventLog.EVENTS.clear();
        PlainWorkerBean.INITIALIZED = false; // 静态演示状态跨测试残留，先复位
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SmartLifecycleConfig.class)) {
            // autoStartup：从未调用 context.start()，refresh 结束即自动启动
            assertTrue(ctx.getBean(StorageLifecycle.class).isRunning(), "autoStartup=true：refresh 后自动 start");
            assertTrue(ctx.getBean(ComputeLifecycle.class).isRunning());
            assertEquals(10, ctx.getBean(StorageLifecycle.class).getPhase());
            assertEquals(20, ctx.getBean(ComputeLifecycle.class).getPhase());
            // 启动顺序：phase 升序（storage=10 先于 compute=20）
            assertTrue(LifecycleEventLog.EVENTS.indexOf("storage:start(phase=10)")
                            < LifecycleEventLog.EVENTS.indexOf("compute:start(phase=20,storage运行中=true)"),
                    "phase 小的先启动");
        }
        // close()：phase 降序停止（compute 先停，且停时依赖的 storage 仍在运行）
        assertEquals(EXPECTED_START_STOP_SCRIPT, LifecycleEventLog.EVENTS, "完整启停剧本");
        System.out.println("[SmartLifecycleTest] 启停剧本: " + LifecycleEventLog.EVENTS);
    }

    @Test
    public void smartInitializingSingletonFiresOnceAfterAllSingletons() {
        LifecycleEventLog.EVENTS.clear();
        AllSingletonsReadyHook.AFTER_SINGLETONS_CALLS.set(0);
        PlainWorkerBean.INITIALIZED = false; // 静态演示状态跨测试残留，先复位
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SmartLifecycleConfig.class)) {
            // 全体非懒单例就绪后只回调一次
            assertEquals(1, AllSingletonsReadyHook.AFTER_SINGLETONS_CALLS.get(), "afterSingletonsInstantiated 恰好一次");
            // 对比 @PostConstruct：hook 自己就绪时 worker 尚未创建；全体就绪回调时 worker 必然已就绪
            assertFalse(AllSingletonsReadyHook.WORKER_READY_AT_POST_CONSTRUCT,
                    "hook 的 @PostConstruct 时刻 worker 尚未就绪（单个 Bean 就绪 ≠ 全体就绪）");
            assertTrue(AllSingletonsReadyHook.WORKER_READY_AT_ALL_SINGLETONS,
                    "afterSingletonsInstantiated 时刻全体单例（含 worker）已就绪");
            assertTrue(PlainWorkerBean.INITIALIZED);
            // 时序：hook 的 @PostConstruct < worker 的 @PostConstruct < afterSingletonsInstantiated
            int iHookPostConstruct = LifecycleEventLog.EVENTS.indexOf("hook:@PostConstruct(worker已就绪=false)");
            int iWorkerPostConstruct = LifecycleEventLog.EVENTS.indexOf("worker:@PostConstruct");
            int iAllSingletons = LifecycleEventLog.EVENTS.indexOf("hook:afterSingletonsInstantiated(worker已就绪=true)");
            assertTrue(iHookPostConstruct < iWorkerPostConstruct && iWorkerPostConstruct < iAllSingletons);
            System.out.println("[SmartLifecycleTest] 单例就绪时序: " + LifecycleEventLog.EVENTS);
        }
    }
}
