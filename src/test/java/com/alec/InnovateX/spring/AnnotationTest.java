package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.annotation.AnnotationCollectorService;
import com.alec.InnovateX.spring.annotation.AnnotationFieldService;
import com.alec.InnovateX.spring.annotation.AnnotationLifecycleBean;
import com.alec.InnovateX.spring.annotation.AnnotationOrderService;
import com.alec.InnovateX.spring.annotation.AnnotationQualifierService;
import com.alec.InnovateX.spring.annotation.AnnotationScanConfig;
import com.alec.InnovateX.spring.annotation.ProviderConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题①注解驱动装配：@ComponentScan / @Autowired / @Qualifier / @Primary / @Resource / @Value / @PostConstruct
 */
public class AnnotationTest {

    @Test
    public void annotationDrivenWiring() {
        AnnotationLifecycleBean.destroyedFlag = false;
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AnnotationScanConfig.class)) {
            // 构造器注入 + @Qualifier 精确指定 bean 名称，绕过 @Primary
            AnnotationOrderService orderService = context.getBean(AnnotationOrderService.class);
            assertEquals("redis", orderService.repositoryType());
            System.out.println("构造器 + @Qualifier 注入结果: " + orderService.createOrder("SO-1001"));

            // 自定义 @Qualifier 元注解：语义化限定符选中 @Persistent 标注的实现（压过 @Primary）
            AnnotationQualifierService qualifierService = context.getBean(AnnotationQualifierService.class);
            assertEquals("redis", qualifierService.repositoryType());
            System.out.println("@Persistent（自定义限定符）注入结果: " + qualifierService.repositoryType()
                    + "（优先级高于 @Primary 的 mysql）");

            // @Resource 按名称 vs @Autowired 按类型（多实现走 @Primary）
            AnnotationFieldService fieldService = context.getBean(AnnotationFieldService.class);
            assertEquals("mysql", fieldService.resourceType());
            assertEquals("mysql", fieldService.autowiredType());
            System.out.println("@Resource(byName)=" + fieldService.resourceType()
                    + "，@Autowired(byType+@Primary)=" + fieldService.autowiredType());

            // 集合注入：List 收集全部实现，Map 的 key 为 bean 名称
            AnnotationCollectorService collector = context.getBean(AnnotationCollectorService.class);
            assertEquals(2, collector.getRepositories().size());
            assertTrue(collector.getRepositoryMap().containsKey("redisMessageRepository"));
            assertTrue(collector.getRepositoryMap().containsKey("mysqlMessageRepository"));
            System.out.println("List 注入: " + collector.getRepositories().size() + " 个实现");
            System.out.println("Map 注入 key: " + collector.getRepositoryMap().keySet());

            // @Value 占位符解析 + 缺失 key 的默认值
            assertEquals("InnovateX", collector.getAppName());
            assertEquals("默认描述", collector.getDesc());

            // required=false：容器中没有该类型 bean 时注入 null，启动不报错
            assertNull(collector.getOptionalHandler());

            // ObjectProvider：按需取/流式取/可选取（required=false 的现代升级版）
            ProviderConsumerService provider = context.getBean(ProviderConsumerService.class);
            assertEquals("mysql", provider.getIfAvailable());
            assertEquals(2, provider.streamAllTypes().size());
            assertEquals("null", provider.getIfMissing());
            System.out.println("ObjectProvider: getIfAvailable=" + provider.getIfAvailable()
                    + "，stream=" + provider.streamAllTypes() + "，缺失类型 getIfAvailable=" + provider.getIfMissing());

            // @PostConstruct 已执行、@PreDestroy 尚未执行
            AnnotationLifecycleBean lifecycleBean = context.getBean(AnnotationLifecycleBean.class);
            assertTrue(lifecycleBean.isInitialized());
            assertFalse(lifecycleBean.isDestroyed());
        }
        // try-with-resources 触发 context.close()，singleton 的 @PreDestroy 被回调
        assertTrue(AnnotationLifecycleBean.destroyedFlag);
        System.out.println("容器关闭后 @PreDestroy 已执行: " + AnnotationLifecycleBean.destroyedFlag);
    }
}
