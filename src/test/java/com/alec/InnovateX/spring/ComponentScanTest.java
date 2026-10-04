package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.component.FaxNotifier;
import com.alec.InnovateX.spring.component.InventoryService;
import com.alec.InnovateX.spring.component.LegacyImportSelector;
import com.alec.InnovateX.spring.component.LocalInventoryStore;
import com.alec.InnovateX.spring.component.MailNotifier;
import com.alec.InnovateX.spring.component.ProdOnlyCondition;
import com.alec.InnovateX.spring.component.ProdStorage;
import com.alec.InnovateX.spring.component.ShippingService;
import com.alec.InnovateX.spring.component.Storage;
import com.alec.InnovateX.spring.component.StorageConfig;
import com.alec.InnovateX.spring.component.CustomTypeFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bean 定义与组件扫描（core/bean 第二课）：
 * - @Component 派生注解与自定义组合注解（meta-annotation）
 * - @ComponentScan 过滤器：ANNOTATION / ASSIGNABLE_TYPE / REGEX / CUSTOM / includeFilters+useDefaultFilters
 * - @Configuration full（CGLIB 拦截 @Bean 方法）vs lite（proxyBeanMethods=false、@Component 内 @Bean）
 * - @Import 普通类与 ImportSelector 批量导入
 * - @Conditional 自定义条件、@Profile 分环境注册、lazyInit 全局懒加载
 */
public class ComponentScanTest {

    private static final String SCAN_PACKAGE = "com.alec.InnovateX.spring.component";

    @Configuration
    @ComponentScan(SCAN_PACKAGE)
    static class ScanAllConfig {
    }

    @Configuration
    @ComponentScan(basePackages = SCAN_PACKAGE, excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InventoryService.class)})
    static class ExcludeByClassConfig {
    }

    @Configuration
    @ComponentScan(basePackages = SCAN_PACKAGE, excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = org.springframework.stereotype.Repository.class)})
    static class ExcludeByAnnotationConfig {
    }

    @Configuration
    @ComponentScan(basePackages = SCAN_PACKAGE, excludeFilters = {
            @ComponentScan.Filter(type = FilterType.CUSTOM, classes = CustomTypeFilter.class)})
    static class ExcludeByCustomFilterConfig {
    }

    @Configuration
    @ComponentScan(basePackages = SCAN_PACKAGE, excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Legacy.*")})
    static class ExcludeByRegexConfig {
    }

    @Configuration
    @ComponentScan(basePackages = SCAN_PACKAGE, useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InventoryService.class))
    static class OnlyInventoryConfig {
    }

    /** 组件扫描基础：@Service/@Repository 与自定义组合注解 @BizComponent 全部被识别；未加注解的支持类不会成 bean。 */
    @Test
    public void componentScanBasics() {
        try (var ctx = new AnnotationConfigApplicationContext(ScanAllConfig.class)) {
            assertEquals("库存充足:SKU-1", ctx.getBean(InventoryService.class).check("SKU-1"));
            assertEquals(100, ctx.getBean(LocalInventoryStore.class).stockOf("SKU-1"));
            // 元注解派生：@BizComponent 自带 @Component，扫描器同样识别
            assertEquals("已发货:杭州", ctx.getBean(ShippingService.class).deliver("杭州"));
            // 未标组件注解的普通类不会被扫描
            assertFalse(ctx.containsBean("customTypeFilter"));
            assertFalse(ctx.containsBean("faxNotifier"));
        }
    }

    /** 元注解原理：ShippingService 类上没有直接的 @Component，但「注解的注解」链路上携带了它。 */
    @Test
    public void metaAnnotationLookup() {
        assertFalse(ShippingService.class.isAnnotationPresent(Component.class),
                "类上直接看没有 @Component");
        assertTrue(AnnotatedElementUtils.hasAnnotation(ShippingService.class, Component.class),
                "沿元注解链路向上找得到 @Component —— 这正是扫描器的查找方式");
    }

    /** excludeFilters + ASSIGNABLE_TYPE：按具体类排除。 */
    @Test
    public void excludeFilterByAssignableType() {
        try (var ctx = new AnnotationConfigApplicationContext(ExcludeByClassConfig.class)) {
            assertFalse(ctx.containsBean("inventoryService"), "按类名排除后不再注册");
            assertTrue(ctx.containsBean("shippingService"), "其余组件不受影响");
        }
    }

    /** excludeFilters + ANNOTATION：按注解排除，@Repository 标注的 bean 全部出局（@Component 本身也是元注解）。 */
    @Test
    public void excludeFilterByAnnotation() {
        try (var ctx = new AnnotationConfigApplicationContext(ExcludeByAnnotationConfig.class)) {
            assertFalse(ctx.containsBean("localInventoryStore"), "带 @Repository 的被注解过滤器排除");
            assertTrue(ctx.containsBean("inventoryService"), "@Service 不受影响");
        }
    }

    /** excludeFilters + CUSTOM：自定义 TypeFilter 排除类名含 Trial 的候选。 */
    @Test
    public void excludeFilterByCustomTypeFilter() {
        try (var ctx = new AnnotationConfigApplicationContext(ExcludeByCustomFilterConfig.class)) {
            assertFalse(ctx.containsBean("trialFeatureService"));
            assertTrue(ctx.containsBean("inventoryService"));
        }
    }

    /** excludeFilters + REGEX：正则匹配的是全限定类名。 */
    @Test
    public void excludeFilterByRegex() {
        try (var ctx = new AnnotationConfigApplicationContext(ExcludeByRegexConfig.class)) {
            assertFalse(ctx.containsBean("legacyHelperService"));
            assertTrue(ctx.containsBean("inventoryService"));
        }
    }

    /** includeFilters + useDefaultFilters=false：白名单模式，只注册显式 include 的组件。 */
    @Test
    public void includeFilterReplacesDefaults() {
        try (var ctx = new AnnotationConfigApplicationContext(OnlyInventoryConfig.class)) {
            assertTrue(ctx.containsBean("inventoryService"));
            assertFalse(ctx.containsBean("shippingService"), "默认过滤器关闭后其余组件不注册");
        }
    }

    record Probe(String tag) {
    }

    @Configuration(proxyBeanMethods = true)
    static class FullConfig {
        @Bean
        Probe probe() {
            return new Probe("full");
        }

        @Bean
        String fullProbe() {
            return probe() == probe() ? "同一实例" : "不同实例";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class LiteConfig {
        @Bean
        Probe liteProbe() {
            return new Probe("lite");
        }

        @Bean
        String liteProbeResult() {
            return liteProbe() == liteProbe() ? "同一实例" : "不同实例";
        }
    }

    /** @Component 中声明 @Bean 也是 lite 模式（无 CGLIB 拦截）。 */
    @Component
    static class LiteComponentConfig {
        @Bean
        Probe componentProbe() {
            return new Probe("component");
        }

        @Bean
        String componentProbeResult() {
            return componentProbe() == componentProbe() ? "同一实例" : "不同实例";
        }
    }

    /** full vs lite：full 模式生成 CGLIB 子类拦截 @Bean 方法保证单例语义；lite 模式直调方法每次 new。 */
    @Test
    public void fullVsLiteConfiguration() {
        try (var ctx = new AnnotationConfigApplicationContext(FullConfig.class)) {
            assertEquals("同一实例", ctx.getBean("fullProbe", String.class),
                    "full：容器内互调 @Bean 方法被 CGLIB 拦截，返回缓存实例");
            assertTrue(ctx.getBean(FullConfig.class).getClass().getName().contains("CGLIB"),
                    "配置类本身被增强为 CGLIB 子类");
        }
        try (var ctx = new AnnotationConfigApplicationContext(LiteConfig.class, LiteComponentConfig.class)) {
            assertEquals("不同实例", ctx.getBean("liteProbeResult", String.class),
                    "lite：proxyBeanMethods=false 后方法直调，各自 new");
            assertEquals("不同实例", ctx.getBean("componentProbeResult", String.class),
                    "lite：@Component 内的 @Bean 同样没有拦截");
            assertFalse(ctx.getBean(LiteConfig.class).getClass().getName().contains("CGLIB"));
        }
    }

    @Configuration
    @Import(LegacyImportSelector.class)
    static class SelectorImportConfig {
        @Bean
        MailNotifier mailNotifier() {
            return new MailNotifier();
        }
    }

    @Configuration
    @Import(FaxNotifier.class)
    static class DirectImportConfig {
    }

    /** @Import 两条路：直接导入普通类；ImportSelector 批量返回类名（类本身无需任何注解）。 */
    @Test
    public void importSelectorAndDirectImport() {
        try (var ctx = new AnnotationConfigApplicationContext(SelectorImportConfig.class)) {
            assertEquals("邮件已发送", ctx.getBean(MailNotifier.class).send());
            assertEquals("传真已发送", ctx.getBean(FaxNotifier.class).send(),
                    "FaxNotifier 没有注解，由 ImportSelector 返回的类名注册成 bean");
        }
        try (var ctx = new AnnotationConfigApplicationContext(DirectImportConfig.class)) {
            assertEquals("传真已发送", ctx.getBean(FaxNotifier.class).send());
        }
    }

    @Configuration
    static class FeatureConfig {
        @Bean
        @Conditional(ProdOnlyCondition.class)
        String prodOnlyFlag() {
            return "prod 专属功能";
        }
    }

    private static AnnotationConfigApplicationContext contextWithEnv(String deployEnv) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("test", Map.of("deploy.env", deployEnv)));
        ctx.register(FeatureConfig.class);
        ctx.refresh();
        return ctx;
    }

    /** @Conditional：Condition 在「注册 bean 定义」阶段评估，条件不满足时定义根本不存在。 */
    @Test
    public void conditionalRegistration() {
        try (var prod = contextWithEnv("prod"); var dev = contextWithEnv("dev")) {
            assertEquals("prod 专属功能", prod.getBean("prodOnlyFlag", String.class));
            assertFalse(dev.containsBean("prodOnlyFlag"), "条件不满足 → 定义从未注册");
        }
    }

    private static AnnotationConfigApplicationContext contextWithProfile(String... profiles) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles(profiles);
        ctx.register(StorageConfig.class);
        ctx.refresh();
        return ctx;
    }

    /** @Profile：按激活 profile 注册对应实现；一个不激活则按类型取 bean 抛异常。 */
    @Test
    public void profileRegistration() {
        try (var prod = contextWithProfile("prod")) {
            assertInstanceOf(ProdStorage.class, prod.getBean(Storage.class));
            assertEquals("生产环境存储(专用集群)", prod.getBean(Storage.class).describe());
        }
        try (var dev = contextWithProfile("dev")) {
            assertEquals("开发环境存储(本地内存)", dev.getBean(Storage.class).describe());
        }
        try (var none = contextWithProfile()) {
            assertThrows(NoSuchBeanDefinitionException.class, () -> none.getBean(Storage.class),
                    "无激活 profile → 两个 Storage 都未注册");
        }
    }

    @Configuration
    @ComponentScan(basePackages = SCAN_PACKAGE, lazyInit = true)
    static class LazyScanConfig {
    }

    /** @ComponentScan(lazyInit=true)：扫描到定义但全部延迟创建；默认（false）refresh 结束即急切实例化。 */
    @Test
    public void lazyComponentScan() {
        int before = InventoryService.createdTotal();
        try (var ctx = new AnnotationConfigApplicationContext(LazyScanConfig.class)) {
            assertEquals(before, InventoryService.createdTotal(), "lazyInit=true：扫描到定义但不创建");
            ctx.getBean(InventoryService.class);
            assertEquals(before + 1, InventoryService.createdTotal());
        }

        int beforeEager = InventoryService.createdTotal();
        try (var ctx = new AnnotationConfigApplicationContext(ScanAllConfig.class)) {
            assertEquals(beforeEager + 1, InventoryService.createdTotal(),
                    "默认急切：refresh 结束单例已创建");
        }
    }
}
