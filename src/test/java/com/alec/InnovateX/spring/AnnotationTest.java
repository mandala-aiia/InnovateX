package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.annotation.AnnotationWiringConfig;
import com.alec.InnovateX.spring.annotation.AuditStorage;
import com.alec.InnovateX.spring.annotation.CollectionInjectionService;
import com.alec.InnovateX.spring.annotation.FieldInjectionService;
import com.alec.InnovateX.spring.annotation.LifecycleShowcaseBean;
import com.alec.InnovateX.spring.annotation.NotificationChannel;
import com.alec.InnovateX.spring.annotation.PinnedInjectionService;
import com.alec.InnovateX.spring.annotation.ProviderLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题①注解驱动装配：@ComponentScan/@PropertySource、@Autowired vs @Resource、@Primary/@Qualifier、
 * 自定义限定符、接口+抽象类多实现、List/Map 集合注入、@Value 占位符、required=false、
 * ObjectProvider、@PostConstruct/@PreDestroy 全链路顺序。
 * 每个 @Test 聚焦一个知识点；try-with-resources 管理 AnnotationConfigApplicationContext
 */
public class AnnotationTest {

    /** LifecycleShowcaseBean 的静态事件表跨测试共享，每个用例前重置，避免互相污染 */
    @BeforeEach
    public void resetSharedState() {
        LifecycleShowcaseBean.EVENTS.clear();
    }

    /** @ComponentScan 扫描注册（bean 名默认类名首字母小写）+ @PropertySource 注入 Environment */
    @Test
    public void componentScanAndPropertySource() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            // 四个实现类都没有显式命名，默认 bean 名能取到 => 扫描注册生效
            assertTrue(ctx.containsBean("emailNotificationChannel"));
            assertTrue(ctx.containsBean("smsNotificationChannel"));
            assertTrue(ctx.containsBean("memoryAuditStorage"));
            assertTrue(ctx.containsBean("redisAuditStorage"));
            // @PropertySource 把 properties 挂进 Environment，占位符才有值可解析
            assertEquals("InnovateX", ctx.getEnvironment().getProperty("annotation.app.name"));
            System.out.println("扫描注册: emailNotificationChannel / smsNotificationChannel / "
                    + "memoryAuditStorage / redisAuditStorage");
            System.out.println("Environment 中 annotation.app.name = " + ctx.getEnvironment().getProperty("annotation.app.name"));
        }
    }

    /** @Resource 按名称 vs @Autowired 按类型（多实现走 @Primary） */
    @Test
    public void resourceByNameVsAutowiredByType() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            FieldInjectionService service = ctx.getBean(FieldInjectionService.class);
            assertEquals("sms", service.getSmsByName());             // @Resource(name) 精确点名
            assertEquals("email", service.getChannelByType());       // @Autowired 按类型 + @Primary
            assertEquals("memory", service.getAuditStorageEngine()); // @Resource 未命中字段名 -> 回退按类型 + @Primary
            System.out.println("@Resource(name)=sms，@Autowired(byType+@Primary)=email，"
                    + "@Resource(回退byType+@Primary)=memory");
        }
    }

    /** @Qualifier 按 bean 名与自定义 @Durable 限定符，均可压过 @Primary 选中非首选实现 */
    @Test
    public void qualifierPinsNonPrimaryImplementation() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            PinnedInjectionService service = ctx.getBean(PinnedInjectionService.class);
            assertEquals("sms", service.getChannelName());       // @Qualifier("smsNotificationChannel")
            assertEquals("redis", service.getStorageEngine());   // @Durable 元限定符（抽象类多实现同样适用）
            System.out.println("构造器 + @Qualifier(bean 名): " + service.notifyUser("alex"));
            System.out.println("构造器 + @Durable(自定义限定符): " + service.audit("订单创建")
                    + "（优先级高于 @Primary）");
        }
    }

    /** List/Map 集合注入：接口与抽象类两套多实现都收集，顺序由 @Order 决定 */
    @Test
    public void collectionInjectionForInterfaceAndAbstract() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            CollectionInjectionService collector = ctx.getBean(CollectionInjectionService.class);
            // List：收集全部实现，@Order(1)/@Order(2) 决定顺序
            assertEquals(List.of("email", "sms"),
                    collector.getChannels().stream().map(NotificationChannel::name).toList());
            assertEquals(List.of("memory", "redis"),
                    collector.getStorages().stream().map(AuditStorage::engine).toList());
            // Map：key 为 bean 名，value 为实例
            assertTrue(collector.getChannelMap().containsKey("emailNotificationChannel"));
            assertTrue(collector.getChannelMap().containsKey("smsNotificationChannel"));
            assertTrue(collector.getStorageMap().containsKey("memoryAuditStorage"));
            assertTrue(collector.getStorageMap().containsKey("redisAuditStorage"));
            System.out.println("List 注入(接口): " + collector.getChannels().size() + " 个 -> "
                    + collector.getChannels().stream().map(NotificationChannel::name).toList());
            System.out.println("List 注入(抽象类): " + collector.getStorages().stream().map(AuditStorage::engine).toList());
            System.out.println("Map 注入 key: " + collector.getChannelMap().keySet());
        }
    }

    /** @Value 占位符解析 + 缺失 key 的默认值兜底 */
    @Test
    public void valuePlaceholderAndDefaultFallback() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            CollectionInjectionService collector = ctx.getBean(CollectionInjectionService.class);
            assertEquals("InnovateX", collector.getAppName());        // properties 里存在
            assertEquals("1.0.0", collector.getAppVersion());         // properties 里存在
            assertEquals("离线亦可测", collector.getSloganOrDefault()); // key 缺失 -> 冒号后默认值
            System.out.println("appName=" + collector.getAppName() + ", appVersion=" + collector.getAppVersion()
                    + ", sloganOrDefault=" + collector.getSloganOrDefault());
        }
    }

    /** @Autowired(required=false)：容器中没有该类型 bean 时注入 null，启动不报错 */
    @Test
    public void optionalDependencyWithRequiredFalse() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            CollectionInjectionService collector = ctx.getBean(CollectionInjectionService.class);
            assertNull(collector.getMetricsBinder());
            System.out.println("required=false 注入缺失类型 MetricsBinder => " + collector.getMetricsBinder());
        }
    }

    /** ObjectProvider：getIfAvailable / orderedStream / stream / getIfAvailable(Supplier) 兜底 */
    @Test
    public void objectProviderOnDemandLookup() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            ProviderLookupService provider = ctx.getBean(ProviderLookupService.class);
            assertEquals("email", provider.primaryChannel());                       // 多候选走 @Primary
            assertEquals(List.of("email", "sms"), provider.orderedChannels());     // 按 @Order 排序
            assertEquals(2, provider.streamCount());                               // 拿到全部实现
            assertEquals("null", provider.missingOrNull());                        // 缺失类型返回 null
            assertEquals("兜底 MetricsBinder", provider.missingWithFallback());    // Supplier 兜底
            System.out.println("ObjectProvider: getIfAvailable=" + provider.primaryChannel()
                    + ", orderedStream=" + provider.orderedChannels()
                    + ", 缺失类型 getIfAvailable=" + provider.missingOrNull()
                    + ", Supplier 兜底=" + provider.missingWithFallback());
        }
    }

    /** @PostConstruct/@PreDestroy 与 InitializingBean/init-method 的完整回调顺序 */
    @Test
    public void lifecycleCallbackOrder() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationWiringConfig.class)) {
            // 容器启动完成后：初始化四连按 1->4 顺序执行完毕
            assertEquals(List.of("1-构造器", "2-@PostConstruct", "3-afterPropertiesSet", "4-customInit"),
                    LifecycleShowcaseBean.EVENTS);
        }
        // try-with-resources 触发 close()：销毁三连按对称反序追加（5->7）
        assertEquals(List.of("1-构造器", "2-@PostConstruct", "3-afterPropertiesSet", "4-customInit",
                "5-@PreDestroy", "6-destroy", "7-customDestroy"), LifecycleShowcaseBean.EVENTS);
        System.out.println("回调全记录: " + LifecycleShowcaseBean.EVENTS);
    }
}
