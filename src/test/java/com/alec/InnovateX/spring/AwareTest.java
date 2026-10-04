package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.aware.AwareSinkBean;
import com.alec.InnovateX.spring.aware.BootstrapConfig;
import com.alec.InnovateX.spring.aware.FeatureImportSelector;
import com.alec.InnovateX.spring.aware.ImportedFeatureConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 主题：Aware 家族。覆盖知识点：
 * 1. 六件套（BeanFactory/Environment/EmbeddedValueResolver/ResourceLoader/
 *    ApplicationEventPublisher/MessageSource Aware）一次性回调且顺序确定；
 * 2. 全部 Aware 回调先于 @PostConstruct；
 * 3. EnvironmentAware 读系统属性；
 * 4. EmbeddedValueResolverAware 解析 ${} 占位符；
 * 5. ImportAware：被 @Import 的配置类拿到导入者元数据（BootstrapConfig @Import + ImportSelector 结构）。
 */
public class AwareTest {

    /** 六件套 + @PostConstruct 的期望回调顺序（Spring 固定行为，可精确断言） */
    private static final List<String> EXPECTED_TRACE = List.of(
            "BeanFactoryAware",
            "EnvironmentAware",
            "EmbeddedValueResolverAware",
            "ResourceLoaderAware",
            "ApplicationEventPublisherAware",
            "MessageSourceAware",
            "@PostConstruct");

    @Test
    public void sixAwareCallbacksInFixedOrderBeforePostConstruct() {
        AwareSinkBean.CALLBACK_TRACE.clear();
        // 从根配置启动：ImportedFeatureConfig 被 @Import 导入，ImportAware 回调才会发生
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BootstrapConfig.class)) {
            AwareSinkBean bean = ctx.getBean(AwareSinkBean.class);

            assertEquals(EXPECTED_TRACE, AwareSinkBean.CALLBACK_TRACE,
                    "六件套按固定顺序回调完成，且全部先于 @PostConstruct");
            System.out.println("[AwareTest] 回调轨迹: " + AwareSinkBean.CALLBACK_TRACE);

            // 六个基础设施对象确实都注入成功
            assertNotNull(bean.getBeanFactory(), "BeanFactoryAware 应注入 BeanFactory");
            assertNotNull(bean.getEnvironment(), "EnvironmentAware 应注入 Environment");
            assertNotNull(bean.getValueResolver(), "EmbeddedValueResolverAware 应注入解析器");
            assertNotNull(bean.getResourceLoader(), "ResourceLoaderAware 应注入 ResourceLoader");
            assertNotNull(bean.getEventPublisher(), "ApplicationEventPublisherAware 应注入发布器");
            assertNotNull(bean.getMessageSource(), "MessageSourceAware 应注入 MessageSource");
        }
    }

    @Test
    public void environmentAwareReadsSystemProperties() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BootstrapConfig.class)) {
            AwareSinkBean bean = ctx.getBean(AwareSinkBean.class);
            // 系统属性是 Environment 的一个 PropertySource：user.dir 必然存在
            assertEquals(System.getProperty("user.dir"), bean.readSystemProperty("user.dir"));
            // 不存在的 key 返回 null（对比 getRequiredProperty 会抛异常）
            assertNull(bean.readSystemProperty("no.such.key.for.aware.demo"));
            System.out.println("[AwareTest] Environment 读系统属性 user.dir = " + bean.readSystemProperty("user.dir"));
        }
    }

    @Test
    public void embeddedValueResolverResolvesPlaceholder() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BootstrapConfig.class)) {
            AwareSinkBean bean = ctx.getBean(AwareSinkBean.class);
            System.setProperty("innovatex.aware.placeholder", "Spring 框架内置能力");
            try {
                String resolved = bean.resolvePlaceholder("Aware 回调注入的是${innovatex.aware.placeholder}!");
                assertEquals("Aware 回调注入的是Spring 框架内置能力!", resolved);
                System.out.println("[AwareTest] 占位符解析: ${innovatex.aware.placeholder} -> Spring 框架内置能力");
            } finally {
                System.clearProperty("innovatex.aware.placeholder");
            }
        }
    }

    @Test
    public void importAwareSeesImporterMetadata() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BootstrapConfig.class)) {
            ImportedFeatureConfig imported = ctx.getBean(ImportedFeatureConfig.class);
            // ImportAware：被导入者拿到的元数据就是导入者（BootstrapConfig）
            assertNotNull(imported.getImportMetadata(), "被 @Import 的配置类应收到 setImportMetadata 回调");
            assertEquals(BootstrapConfig.class.getName(), imported.getImportMetadata().getClassName());
            System.out.println("[AwareTest] ImportAware 导入者 = " + imported.getImportMetadata().getClassName());

            // ImportSelector 的互补视角：selectImports 入参是导入它的 ImportedFeatureConfig 的元数据
            assertEquals(ImportedFeatureConfig.class.getName(), FeatureImportSelector.importerSeenBySelector);
            System.out.println("[AwareTest] ImportSelector 看到的导入者 = " + FeatureImportSelector.importerSeenBySelector);
        }
    }
}
