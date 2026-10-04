package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.aot.AotDemoConfig;
import com.alec.InnovateX.spring.aot.LegacyHintsRegistrar;
import com.alec.InnovateX.spring.aot.LegacyService;
import com.alec.InnovateX.spring.aot.RemoteCallback;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AOT RuntimeHints：Registrar 登记反射/资源/序列化/动态代理四类 hint，
 * 并对照真实反射用法验证登记与用法一致；@ImportRuntimeHints 在 JVM 运行时 inert。
 * hint 断言也可以用 RuntimeHintsPredicates（流式 Predicate），这里直接查询
 * RuntimeHints 的内部结构，路径更透明
 */
public class AotHintsTest {

    private RuntimeHints registeredHints() {
        RuntimeHints hints = new RuntimeHints();
        new LegacyHintsRegistrar().registerHints(hints, getClass().getClassLoader());
        return hints;
    }

    @Test
    public void reflectionHintsRegisteredForLegacyService() throws Exception {
        RuntimeHints hints = registeredHints();
        TypeHint typeHint = hints.reflection().getTypeHint(LegacyService.class);
        assertNotNull(typeHint, "应为 LegacyService 注册类型级反射 hint");
        assertTrue(typeHint.getMemberCategories().contains(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
        assertTrue(typeHint.getMemberCategories().contains(MemberCategory.INVOKE_DECLARED_METHODS));

        // 登记对应的真实反射用法：包私有构造器 + 私有方法。
        // 普通 JVM 下反射照常能跑——hints 是给 AOT/native 构建期看的，不影响运行时行为
        Constructor<LegacyService> ctor = LegacyService.class.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        LegacyService service = ctor.newInstance("aot");
        Method hidden = LegacyService.class.getDeclaredMethod("hiddenTransform", String.class);
        hidden.setAccessible(true);
        assertEquals("[aot] DEMO", hidden.invoke(service, "demo"));
        System.out.println("反射 hint 登记 + 真实反射调用: " + hidden.invoke(service, "demo"));
    }

    @Test
    public void resourceBundleAndProxyHints() {
        RuntimeHints hints = registeredHints();
        // 资源 pattern：i18n 资源束保留（ResourcePatternHints 按 includes 组织）
        assertTrue(hints.resources().resourcePatternHints()
                        .flatMap(container -> container.getIncludes().stream())
                        .anyMatch(include -> "message_*.properties".equals(include.getPattern())),
                "应登记 message_*.properties 资源 pattern");
        // 资源束：按 basename 注册（JDK 序列化通道已在 7.x 标记待删除，故不演示）
        assertTrue(hints.resources().resourceBundleHints()
                        .anyMatch(bundle -> "message".equals(bundle.getBaseName())),
                "应登记 basename=message 的 ResourceBundle hint");
        // JDK 动态代理：接口可达
        assertTrue(hints.proxies().jdkProxyHints()
                        .anyMatch(jdk -> jdk.getProxiedInterfaces().stream()
                                .map(TypeReference::getName).toList()
                                .contains(RemoteCallback.class.getName())),
                "应登记 RemoteCallback 的 JDK 代理 hint");
        System.out.println("资源/资源束/代理 hint 均已登记");
    }

    @Test
    public void importRuntimeHintsInertOnJvm() {
        // JVM 运行时 @ImportRuntimeHints 不执行登记逻辑：上下文照常构建，Bean 照常可用
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AotDemoConfig.class)) {
            assertEquals("aot-demo", ctx.getBean("aotDemoAnchor", String.class));
            System.out.println("@ImportRuntimeHints 在 JVM 运行时 inert：上下文照常启动，AOT 处理阶段才读取");
        }
    }
}
