package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.aware.AllAwareBean;
import com.alec.InnovateX.spring.aware.AppApplicationContextAware;
import com.alec.InnovateX.spring.aware.AppBeanNameAware;
import com.alec.InnovateX.spring.aware.AwareConfig;
import com.alec.InnovateX.spring.aware.RootConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑦Aware 全家桶：6 个基础设施注入回调 + @Configuration 专属的 ImportAware
 */
public class AwareTest {

    @Test
    public void xmlAwareBeans() {
        // XML 版 Aware：回调打印在控制台（setBeanName/setApplicationContext），按名装配成功即证明回调已触发
        try (org.springframework.context.support.GenericApplicationContext context = XmlContexts.load()) {
            assertNotNull(context.getBean("appBeanNameAware", AppBeanNameAware.class));
            assertNotNull(context.getBean(AppApplicationContextAware.class));
            System.out.println("XML Aware Bean 装配成功（回调打印见控制台）");
        }
    }

    @Test
    public void allAwareCallbacks() {
        AllAwareBean.CALLBACK_ORDER.clear();
        // 从 RootConfig 启动：AwareConfig 被 @Import 导入，ImportAware 回调才会发生
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(RootConfig.class)) {
            AllAwareBean bean = ctx.getBean(AllAwareBean.class);

            // 六个基础设施对象全部由容器回调注入
            assertNotNull(bean.getBeanFactory());
            assertNotNull(bean.getEnvironment());
            assertNotNull(bean.getEventPublisher());
            assertNotNull(bean.getMessageSource());
            assertNotNull(bean.getResourceLoader());
            assertNotNull(bean.getEmbeddedValueResolver());
            System.out.println("Aware 回调顺序: " + AllAwareBean.CALLBACK_ORDER);

            // EnvironmentAware 拿到的 Environment 能读系统属性（含 profile 操作）
            assertNotNull(bean.getEnvironment().getProperty("user.dir"));

            // EmbeddedValueResolverAware 的解析器就是 @Value ${} 占位符的底层机制
            System.setProperty("aware.demo.name", "InnovateX");
            try {
                assertEquals("hello InnovateX", bean.getEmbeddedValueResolver().resolveStringValue("hello ${aware.demo.name}"));
                System.out.println("StringValueResolver 解析占位符: hello ${aware.demo.name} -> hello InnovateX");
            } finally {
                System.clearProperty("aware.demo.name");
            }

            // ImportAware：AwareConfig 被 RootConfig @Import 导入，拿到的元数据就是导入者
            assertNotNull(ctx.getBean(AwareConfig.class).getImportMetadata());
            assertEquals(RootConfig.class.getName(),
                    ctx.getBean(AwareConfig.class).getImportMetadata().getClassName());
            assertTrue(AllAwareBean.CALLBACK_ORDER.contains("EmbeddedValueResolverAware"));
        }
    }
}
