package com.alec.InnovateX.spring.aot;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * RuntimeHintsRegistrar：把"运行期才会发生"的反射/资源/序列化/动态代理提前登记。
 *
 * 关键认知：这些 hint 在普通 JVM 运行时是 inert 的（反射照常能跑，不受影响）；
 * 它们只在 AOT 处理阶段（spring-core 的 ContextAotProcessor / GraalVM native-image
 * 构建）被读取，决定哪些类/成员/资源在产物中保留 reachable。
 * Spring 6 引入、7.x 全面深化的 AOT 运行时模型，是 Boot 3 native image
 * "毫秒级启动"的底层机制——纯 Framework 项目同样可以（也应该）为将来 AOT 化登记 hints
 */
public class LegacyHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // ① 反射：类型级声明。分类精确到"允许调声明的构造器/方法"，比一刀切 INTROSPECT 更小权限
        hints.reflection().registerType(LegacyService.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        // ② 资源：classpath 匹配 pattern 的资源要在产物里保留（i18n 资源束是典型场景）
        hints.resources().registerPattern("message_*.properties");

        // ③ 资源束：按 basename 注册 ResourceBundle（i18n 的官方姿势，比 pattern 更语义化）。
        // 注：JDK 序列化通道 hints.serialization() 在 7.x 已 @Deprecated(forRemoval)——
        // Spring 官方在推动弃用 JDK 序列化，新代码不要再依赖它
        hints.resources().registerResourceBundle("message");

        // ④ JDK 动态代理：运行期 Proxy.newProxyInstance 生成的接口也要提前声明
        hints.proxies().registerJdkProxy(RemoteCallback.class);
    }
}
