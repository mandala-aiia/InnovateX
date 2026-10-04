package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.di.AliPayProcessor;
import com.alec.InnovateX.spring.di.AppInfo;
import com.alec.InnovateX.spring.di.CatalogService;
import com.alec.InnovateX.spring.di.CheckoutService;
import com.alec.InnovateX.spring.di.CtorA;
import com.alec.InnovateX.spring.di.CtorB;
import com.alec.InnovateX.spring.di.EmailSender;
import com.alec.InnovateX.spring.di.FieldInjectedService;
import com.alec.InnovateX.spring.di.MultiCtorService;
import com.alec.InnovateX.spring.di.NotifierHub;
import com.alec.InnovateX.spring.di.OptionalDepsService;
import com.alec.InnovateX.spring.di.OrderRepo;
import com.alec.InnovateX.spring.di.PaymentProcessor;
import com.alec.InnovateX.spring.di.PaymentService;
import com.alec.InnovateX.spring.di.PricingClient;
import com.alec.InnovateX.spring.di.PushSender;
import com.alec.InnovateX.spring.di.RepoFacade;
import com.alec.InnovateX.spring.di.ReportService;
import com.alec.InnovateX.spring.di.ResourceInjectedService;
import com.alec.InnovateX.spring.di.SetterA;
import com.alec.InnovateX.spring.di.SetterB;
import com.alec.InnovateX.spring.di.SmsSender;
import com.alec.InnovateX.spring.di.UserRepo;
import com.alec.InnovateX.spring.di.WeChatPayProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 依赖注入（core/bean 第三课）：
 * - 构造器（唯一构造器免注解 / 多构造器 @Autowired）、Setter、字段注入
 * - 多候选消歧：@Primary、@Qualifier、@Resource（先名后型，与 @Autowired 相反）
 * - 可选依赖：Optional / @Nullable / required=false / ObjectProvider 兜底
 * - 集合注入：List 按 @Order 排序、Map 以 bean 名为 key；泛型注入按类型参数精确命中
 * - @Value：占位符、默认值、SpEL
 * - 循环依赖：构造器无解；setter 循环默认被拒（Spring 6.1+ 默认关闭），显式放开后三级缓存可解
 */
public class DependencyInjectionTest {

    @Configuration
    static class BasicConfig {
        @Bean
        CatalogService catalogService() {
            return new CatalogService();
        }
    }

    /** 构造器注入：唯一构造器无需任何注解；多构造器时用 @Autowired 指定。 */
    @Test
    public void constructorInjection() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(BasicConfig.class);
            ctx.registerBean(CheckoutService.class);
            ctx.registerBean(MultiCtorService.class);
            ctx.refresh();

            assertEquals("价:书", ctx.getBean(CheckoutService.class).quote("书"));
            assertEquals("目录构造:价:书", ctx.getBean(MultiCtorService.class).label(),
                    "两个构造器中容器选了 @Autowired 标注的那个");
        }
    }

    /** Setter 与字段注入：能工作，但官方推荐构造器注入（依赖显式、可测试、可 final）。 */
    @Test
    public void setterAndFieldInjection() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(BasicConfig.class);
            ctx.registerBean(ReportService.class);
            ctx.registerBean(FieldInjectedService.class);
            ctx.refresh();

            assertEquals("报表[价:书]", ctx.getBean(ReportService.class).report("书"));
            assertEquals("价:书", ctx.getBean(FieldInjectedService.class).quote("书"));
        }
    }

    @Configuration
    static class PaymentConfig {
        @Bean
        @Primary
        AliPayProcessor aliPayProcessor() {
            return new AliPayProcessor();
        }

        @Bean
        WeChatPayProcessor weChatPayProcessor() {
            return new WeChatPayProcessor();
        }

        @Bean
        PaymentService paymentService(@Qualifier("weChatPayProcessor") PaymentProcessor chosen) {
            return new PaymentService(chosen);
        }
    }

    /** @Primary 与 @Qualifier：不点名时走主候选；点名字（含 @Bean 方法参数上）精确命中。 */
    @Test
    public void primaryAndQualifier() {
        try (var ctx = new AnnotationConfigApplicationContext(PaymentConfig.class)) {
            // 同类型两个 bean，但存在 @Primary → 按类型获取不再抛 NoUnique
            assertInstanceOf(AliPayProcessor.class, ctx.getBean(PaymentProcessor.class));
            assertEquals("微信支付:100分", ctx.getBean(PaymentService.class).chosenWay());
            assertEquals("支付宝支付:100分", ctx.getBean(PaymentService.class).defaultWay());
        }
    }

    /** @Resource：先按名字找，字段名不是 bean 名时回退按类型（遵守 @Primary）。 */
    @Test
    public void resourceAnnotation() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(PaymentConfig.class);
            ctx.registerBean(ResourceInjectedService.class);
            ctx.refresh();

            ResourceInjectedService service = ctx.getBean(ResourceInjectedService.class);
            assertEquals("微信支付:100分", service.namedWay(), "按 name 点名微信渠道");
            assertEquals("支付宝支付:100分", service.defaultWay(),
                    "字段名 aliPay 不是 bean 名 → 回退按类型 → 命中 @Primary");
        }
    }

    /** 可选依赖四写法：Optional.empty / null / 跳过注入 / ObjectProvider 惰性句柄 + 兜底。 */
    @Test
    public void optionalDependencies() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(OptionalDepsService.class);
            ctx.refresh();
            OptionalDepsService service = ctx.getBean(OptionalDepsService.class);

            assertTrue(service.optionalClient().isEmpty(), "Optional 注入不存在依赖 → empty");
            assertNull(service.nullableClient(), "@Nullable → 注入 null");
            assertNull(service.absentClient(), "required=false → 跳过注入");
            assertNull(service.clientProvider().getIfAvailable(), "provider 查询不存在 → null");
            assertEquals("pricing-ok", service.pingWithFallback(), "getIfAvailable(Supplier) 提供兜底");
        }
        // 注册之后 provider 立刻可用 —— 适合「增强型」依赖
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(PricingClient.class);
            ctx.registerBean(OptionalDepsService.class);
            ctx.refresh();
            assertEquals("pricing-ok", ctx.getBean(OptionalDepsService.class).clientProvider().getObject().ping());
        }
    }

    @Configuration
    static class NotifierConfig {
        @Bean
        EmailSender emailSender() {
            return new EmailSender();
        }

        @Bean
        SmsSender smsSender() {
            return new SmsSender();
        }

        @Bean
        PushSender pushSender() {
            return new PushSender();
        }
    }

    /** 集合注入：List 按 @Order 升序；Map 的 key 是 bean 名。 */
    @Test
    public void collectionInjection() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(NotifierConfig.class);
            ctx.registerBean(NotifierHub.class);
            ctx.refresh();
            NotifierHub hub = ctx.getBean(NotifierHub.class);

            assertEquals(java.util.List.of("sms", "email", "push"), hub.channels(),
                    "@Order(1)/@Order(2)/@Order(3) 决定 List 顺序");
            assertTrue(hub.beanNames().containsAll(java.util.Set.of("smsSender", "emailSender", "pushSender")),
                    "Map 的 key 是 bean 名");
            assertEquals("email", hub.channelOf("emailSender"));
        }
    }

    /** 泛型注入：BaseRepo<User> 与 BaseRepo<Order> 两个候选，凭泛型参数各自命中，无需 @Qualifier。 */
    @Test
    public void genericInjection() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(UserRepo.class);
            ctx.registerBean(OrderRepo.class);
            ctx.registerBean(RepoFacade.class);
            ctx.refresh();

            RepoFacade facade = ctx.getBean(RepoFacade.class);
            assertEquals("UserRepo", facade.userRepoName());
            assertEquals("OrderRepo", facade.orderRepoName());
        }
    }

    @Configuration
    @PropertySource("classpath:di/app.properties")
    static class ValueConfig {
        @Bean
        AppInfo appInfo() {
            return new AppInfo();
        }
    }

    /** @Value：${} 占位符、: 默认值、#{} SpEL 字面量、#{$然后SpEL} 混合。 */
    @Test
    public void valueAnnotation() {
        try (var ctx = new AnnotationConfigApplicationContext(ValueConfig.class)) {
            AppInfo info = ctx.getBean(AppInfo.class);
            assertEquals("InnovateX-Lab", info.getName());
            assertEquals("7.0.9", info.getVersion());
            assertEquals("UTC", info.getZone(), "app.zone 未定义 → 取默认值");
            assertEquals(42, info.getMagic(), "#{2 * 21} 是 SpEL 字面量运算");
            assertEquals(9, info.getPoolPlusOne(), "#{${app.threadPool} + 1} → 占位符先解析为 8 再加 1");
        }
    }

    /** 构造器循环依赖：两个 bean 都在「创建中」互相等待，三级缓存也救不了 → 直接失败。 */
    @Test
    public void constructorCycleAlwaysFails() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(CtorA.class);
            ctx.registerBean(CtorB.class);
            BeanCreationException ex = assertThrows(BeanCreationException.class, ctx::refresh);
            assertTrue(causeChainContains(ex, BeanCurrentlyInCreationException.class),
                    "根因应是 BeanCurrentlyInCreationException，实际: " + ex);
        }
    }

    /**
     * Setter 循环依赖（框架默认）：AbstractAutowireCapableBeanFactory.allowCircularReferences 默认 true，
     * 对象先创建、后注入，靠三级缓存（提前暴露早期引用）互相注入成功。
     * 注意：Spring Boot 2.6+ 把默认改为 false，但那是 Boot 的装配默认，裸框架没变。
     */
    @Test
    public void setterCycleResolvedByDefault() {
        var ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(SetterA.class);
        ctx.registerBean(SetterB.class);
        ctx.refresh();
        try {
            SetterA a = ctx.getBean(SetterA.class);
            SetterB b = ctx.getBean(SetterB.class);
            assertSame(b, a.getB());
            assertSame(a, b.getA(), "互相持有的是容器里的同一对单例");
        } finally {
            ctx.close();
        }
    }

    /** 显式关闭 allowCircularReferences（Spring Boot 的默认值）：setter 循环也直接失败。 */
    @Test
    public void setterCycleRejectedWhenDisabled() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            // setAllowCircularReferences 定义在 DefaultListableBeanFactory 上，不在接口上
            ((org.springframework.beans.factory.support.DefaultListableBeanFactory) ctx.getBeanFactory())
                    .setAllowCircularReferences(false);
            ctx.registerBean(SetterA.class);
            ctx.registerBean(SetterB.class);
            BeanCreationException ex = assertThrows(BeanCreationException.class, ctx::refresh);
            assertTrue(causeChainContains(ex, BeanCurrentlyInCreationException.class),
                    "关闭后根因是 BeanCurrentlyInCreationException，实际: " + ex);
        }
    }

    private static boolean causeChainContains(Throwable ex, Class<? extends Throwable> type) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }
}
