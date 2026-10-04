package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.aware.AllAwareBean;
import com.alec.InnovateX.spring.aware.AltAccessBean;
import com.alec.InnovateX.spring.aware.AwareLog;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Aware 回调（context 能力注入）：
 * - 8 个常用 Aware 接口的精确触发顺序（容器核心三件套在前、上下文能力五件在后）
 * - 各回调注入的能力都真实可用：Environment / ResourceLoader / 值解析器 / 事件发布器 / 容器引用
 * - 替代方案：直接用构造器注入 ApplicationContext / ObjectProvider，比 Aware 更直观、更好测试
 */
public class AwareTest {

    /** 收件人监听器：配合 ApplicationEventPublisherAware 演示「拿到的发布器真的能发事件」。 */
    public static class CapturingListener {

        public static final List<String> RECEIVED = new ArrayList<>();

        @EventListener
        public void on(String message) {
            RECEIVED.add(message);
        }
    }

    /** 触发顺序：BeanName → BeanClassLoader → BeanFactory（容器直接调）→ Environment → EmbeddedValueResolver
     *  → ResourceLoader → ApplicationEventPublisher → ApplicationContext（AwareProcessor 调）→ 初始化回调。 */
    @Test
    public void awareInvocationOrder() {
        AwareLog.reset();
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(AllAwareBean.class);
            ctx.refresh();
            assertEquals(List.of(
                    "BeanNameAware",
                    "BeanClassLoaderAware",
                    "BeanFactoryAware",
                    "EnvironmentAware",
                    "EmbeddedValueResolverAware",
                    "ResourceLoaderAware",
                    "ApplicationEventPublisherAware",
                    "ApplicationContextAware",
                    "afterPropertiesSet（Aware 全部就绪后才轮到初始化回调）"), AwareLog.events());
        }
    }

    /** 注入的引用与容器一致、能力真实可用：环境、类加载器、BeanFactory、资源、占位符解析、事件发布。 */
    @Test
    public void injectedCapabilitiesAreReal() throws Exception {
        CapturingListener.RECEIVED.clear();
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(AllAwareBean.class);
            ctx.registerBean(CapturingListener.class);
            ctx.refresh();
            AllAwareBean bean = ctx.getBean(AllAwareBean.class);

            assertEquals("allAwareBean", bean.getBeanName());
            assertSame(ctx, bean.getApplicationContext(), "拿到的就是当前容器");
            assertSame(ctx.getBeanFactory(), bean.getBeanFactory());
            assertSame(ctx.getEnvironment(), bean.getEnvironment());
            assertSame(bean.getClass().getClassLoader(), bean.getClassLoader());

            // ResourceLoaderAware：加载 classpath 资源
            Resource resource = bean.getResourceLoader().getResource("classpath:aware/note.txt");
            assertTrue(resource.exists());
            assertEquals("note.txt", resource.getFilename());
            assertEquals("Aware 主题占位资源", resource.getContentAsString(StandardCharsets.UTF_8).trim());

            // EmbeddedValueResolverAware：解析占位符（底层就是 Environment）
            assertEquals(System.getProperty("user.dir"), bean.resolve("${user.dir}"));

            // ApplicationEventPublisherAware：发布的事件被监听器收到
            bean.publish("你好事件");
            assertEquals(List.of("你好事件"), CapturingListener.RECEIVED);
        }
    }

    /** 替代方案：容器能力可以直接当依赖注入（构造器 / ObjectProvider），无需 Aware 回调。 */
    @Test
    public void constructorInjectionAlternative() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(AllAwareBean.class);
            ctx.registerBean(AltAccessBean.class);
            ctx.refresh();
            AltAccessBean alt = ctx.getBean(AltAccessBean.class);

            assertEquals("allAwareBean", alt.beanNameOf(AllAwareBean.class));
            assertSame(ctx.getEnvironment(), alt.environment());
        }
    }
}
