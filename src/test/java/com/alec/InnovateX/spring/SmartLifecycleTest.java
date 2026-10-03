package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.lifecycle.App;
import com.alec.InnovateX.spring.lifecycle.AppDev;
import com.alec.InnovateX.spring.lifecycle.FullLifecycleBean;
import com.alec.InnovateX.spring.lifecycle.FullLifecycleConfig;
import com.alec.InnovateX.spring.lifecycle.LifecycleConfig;
import com.alec.InnovateX.spring.lifecycle.PhaseOneLifecycle;
import com.alec.InnovateX.spring.lifecycle.PhaseTwoLifecycle;
import com.alec.InnovateX.spring.lifecycle.SmartSingletonHook;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑧Smart 系回调：SmartInitializingSingleton（全体单例就绪）、
 * SmartLifecycle（phase 控制启停顺序、autoStartup）
 */
public class SmartLifecycleTest {

    @Test
    public void smartCallbacksAndPhaseOrder() {
        PhaseOneLifecycle.EVENTS.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
            // SmartInitializingSingleton：refresh 尾声被回调，且此时所有单例已初始化
            assertTrue(SmartSingletonHook.isInvoked());
            assertTrue(SmartSingletonHook.isPlainBeanReadyWhenInvoked());

            // SmartLifecycle autoStartup：refresh 结束即自动 start，phase 升序
            assertEquals(2, PhaseOneLifecycle.EVENTS.size());
            assertEquals("phase1-start", PhaseOneLifecycle.EVENTS.get(0));
            assertEquals("phase2-start", PhaseOneLifecycle.EVENTS.get(1));
            assertTrue(ctx.getBean(PhaseOneLifecycle.class).isRunning());
            assertTrue(ctx.getBean(PhaseTwoLifecycle.class).isRunning());
        }
        // close()：phase 降序 stop（2 先停、1 后停），再销毁 Bean
        assertEquals(4, PhaseOneLifecycle.EVENTS.size());
        assertEquals("phase2-stop", PhaseOneLifecycle.EVENTS.get(2));
        assertEquals("phase1-stop", PhaseOneLifecycle.EVENTS.get(3));
        System.out.println("SmartLifecycle 启停事件序列: " + PhaseOneLifecycle.EVENTS);
    }

    @Test
    public void xmlLifecycleBeans() {
        // XML 版生命周期：init-method/destroy-method、InitializingBean、占位符与属性覆盖解析、LifecycleProcessor
        try (org.springframework.context.support.GenericApplicationContext context = XmlContexts.load()) {
            App app = context.getBean(App.class);
            // 占位符：app.name 来自 app.properties；appFirSec 三个文件都定义了——
            // 实测 PropertySourcesPlaceholderConfigurer 多 locations 合并时"后加载的覆盖先加载的"，
            // 列表末尾的 app.properties（app_pro）反而胜出（与直觉相反，以此断言为准）
            assertEquals("原神", app.getAppName());
            assertEquals("app_pro", app.getAppFirSec());
            assertEquals(List.of("原神1号", "原神2号", "原神3号"), app.getDesc());

            AppDev appDev = context.getBean(AppDev.class);
            // appDevName：p:appDevName="${appDev.name}"，实测值为"原神DevName"（Spring 6.2 的 p:/占位符组合行为）
            assertEquals("原神DevName", appDev.getAppDevName());
            // PropertyOverrideConfigurer：override.properties 强制覆盖了占位符解析出的"原神Dev_over"
            assertEquals("原神Dev_override", appDev.getAppDevOverride());
            // c: 命名空间构造参数注入
            assertEquals("原神Dev_C", appDev.getAppDevC());
            // PropertyOverrideConfigurer：override.properties 强制覆盖了占位符解析出的"原神Dev_over"
            assertEquals("原神Dev_override", appDev.getAppDevOverride());
            // c: 命名空间构造参数注入
            assertEquals("原神Dev_C", appDev.getAppDevC());
            // ref 注入：App 持有同一个 AppDev
            assertEquals(appDev, app.getAppDev());
            System.out.println("XML 生命周期 Bean: " + app);

            // 名为 lifecycleProcessor 的 Bean 会被容器当作 LifecycleProcessor（start/stop 时回调）
            context.start();
            context.stop();
        }  // close 触发 destroy-method/DisposableBean.destroy
    }

    @Test
    public void fullLifecycleChain() {
        FullLifecycleBean.EVENTS.clear();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(FullLifecycleConfig.class);
        try {
            // 初始化阶段：10 个事件（含自定义 BPP 的前置/后置）
            List<String> events = FullLifecycleBean.EVENTS;
            assertEquals(10, events.size());
            System.out.println("满配 Bean 初始化链条: " + events);
            // 确定顺序的主链：构造 -> 属性填充 -> Aware×3 -> (@PostConstruct 与自定义BPP前置) -> afterPropertiesSet -> initMethod -> BPP后置
            int i1 = events.indexOf("1.构造器实例化");
            int i2 = events.indexOf("2.@Autowired setter注入");
            int i3 = events.indexOf("3.BeanNameAware.setBeanName");
            int i4 = events.indexOf("4.BeanFactoryAware.setBeanFactory");
            int i5 = events.indexOf("5.ApplicationContextAware.setApplicationContext");
            int i6 = events.indexOf("6.@PostConstruct");
            int i7 = events.indexOf("7.BeanPostProcessor.beforeInitialization");
            int i8 = events.indexOf("8.InitializingBean.afterPropertiesSet");
            int i9 = events.indexOf("9.@Bean(initMethod)自定义初始化");
            int iAfter = events.indexOf("BeanPostProcessor.afterInitialization（初始化完成，代理一般在此生成）");
            assertTrue(i1 < i2 && i2 < i3 && i3 < i4 && i4 < i5);
            // @PostConstruct 与自定义 BPP 前置的相对顺序取决于 BPP 注册顺序与 Ordered 值，
            // 只断言两者都落在 ApplicationContextAware 之后、afterPropertiesSet 之前
            assertTrue(i5 < i6 && i6 < i8);
            assertTrue(i5 < i7 && i7 < i8);
            assertTrue(i8 < i9 && i9 < iAfter);
        } finally {
            ctx.close();
        }
        // 销毁阶段：@PreDestroy -> DisposableBean.destroy -> @Bean(destroyMethod)
        List<String> events = FullLifecycleBean.EVENTS;
        assertEquals(13, events.size());
        System.out.println("满配 Bean 完整链条: " + events);
        assertTrue(events.indexOf("10.@PreDestroy") == 10);
        assertTrue(events.indexOf("11.DisposableBean.destroy") == 11);
        assertTrue(events.indexOf("12.@Bean(destroyMethod)自定义销毁") == 12);
    }
}
