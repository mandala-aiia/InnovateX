package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.extension.ExtensionConfig;
import com.alec.InnovateX.spring.extension.ExtensionTimeline;
import com.alec.InnovateX.spring.extension.OnDemandService;
import com.alec.InnovateX.spring.extension.ReportDocument;
import com.alec.InnovateX.spring.extension.ReportDocumentFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题：容器扩展点。覆盖知识点：
 * 1. FactoryBean：按名/按产品类型取"产品"，& 前缀/按工厂类型取"工厂本身"，isSingleton 缓存产品；
 * 2. BFPP 改写 BeanDefinition（lazy-init false 翻 true）：refresh 后不实例化、getBean 才构造；
 * 3. BeanPostProcessor / InstantiationAwareBeanPostProcessor / BFPP 三类后处理器在正确环节生效；
 * 4. 后处理器必须 static @Bean 注册的原因（见 ExtensionConfig 注释）。
 */
public class ExtensionTest {

    @Test
    public void factoryBeanProductVersusFactoryItself() {
        ReportDocumentFactoryBean.PRODUCT_CREATIONS.set(0);
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ExtensionConfig.class)) {
            // 按名字（不带 &）：拿到的不是工厂，而是 getObject() 的产品
            Object byName = ctx.getBean("reportDocumentFactory");
            ReportDocument product = assertInstanceOf(ReportDocument.class, byName,
                    "按名取 FactoryBean 注册名，得到的是产品");
            assertEquals("InnovateX-年度报告", product.getTitle());

            // 按产品类型：同样解析到产品，且是同一个实例
            ReportDocument byProductType = ctx.getBean(ReportDocument.class);
            assertSame(byName, byProductType, "isSingleton=true：产品被缓存，多次获取同一实例");

            // "&" 前缀 / 按工厂类型：拿到工厂本身（需自行 getObject()）
            Object factoryByAmpersand = ctx.getBean("&reportDocumentFactory");
            assertInstanceOf(ReportDocumentFactoryBean.class, factoryByAmpersand);
            assertSame(factoryByAmpersand, ctx.getBean(ReportDocumentFactoryBean.class));

            // 单例产品只生产一次
            assertEquals(1, ReportDocumentFactoryBean.PRODUCT_CREATIONS.get(),
                    "isSingleton=true：多次 getBean 只触发一次 getObject()");
            System.out.println("[ExtensionTest] 产品=" + product + "，工厂=" + factoryByAmpersand
                    + "，getObject 调用次数=" + ReportDocumentFactoryBean.PRODUCT_CREATIONS.get());
        }
    }

    @Test
    public void beanFactoryPostProcessorFlipsLazyInit() {
        ExtensionTimeline.TIMELINE.clear();
        OnDemandService.INSTANTIATED = false;
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ExtensionConfig.class)) {
            // BFPP 已在"定义就绪后、实例化前"把 lazy-init 翻成 true：定义层可查
            assertTrue(ctx.getBeanFactory().getBeanDefinition("onDemandService").isLazyInit(),
                    "BFPP 改写后定义应为 lazy-init=true");
            // 时序断言：refresh 结束时 BFPP 已执行、但目标 Bean 尚未构造
            assertEquals(List.of("bfpp:lazy-init已翻为true"), ExtensionTimeline.TIMELINE,
                    "refresh 期间只有 BFPP 一条事件");
            assertFalse(OnDemandService.INSTANTIATED, "被改成 lazy 后 refresh 不应实例化目标 Bean");

            // 第一次 getBean 才真正构造
            assertEquals("pong:按需服务", ctx.getBean(OnDemandService.class).ping());
            assertTrue(OnDemandService.INSTANTIATED, "getBean 时才构造");
            System.out.println("[ExtensionTest] BFPP 改写生效: refresh 后不实例化，getBean 时才构造");
        }
    }

    @Test
    public void threePostProcessorFamiliesInterceptAtRightPhases() {
        ExtensionTimeline.TIMELINE.clear();
        OnDemandService.INSTANTIATED = false;
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ExtensionConfig.class)) {
            ctx.getBean(OnDemandService.class);
            // 一条时间线看全三类扩展点的管辖区间：
            // BFPP（定义层）→ IABPP 实例化前 → 构造 → IABPP 实例化后/属性前 → IABPP 属性加工 → BPP 初始化前 → BPP 初始化后
            List<String> expected = List.of(
                    "bfpp:lazy-init已翻为true",
                    "iabpp:postProcessBeforeInstantiation",
                    "onDemandService:构造器",
                    "iabpp:postProcessAfterInstantiation",
                    "iabpp:postProcessProperties",
                    "bpp:postProcessBeforeInitialization",
                    "bpp:postProcessAfterInitialization");
            assertEquals(expected, ExtensionTimeline.TIMELINE, "三类后处理器按固定环节依次介入");
            System.out.println("[ExtensionTest] 扩展点时间线: " + ExtensionTimeline.TIMELINE);
        }
    }
}
