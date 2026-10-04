package com.alec.InnovateX.spring.aot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * @ImportRuntimeHints：把 Registrar 挂到配置类上。
 * 普通 JVM 启动时它是 inert 的（上下文照常构建，不执行登记逻辑）；
 * AOT 处理阶段，spring-context 会收集所有 @ImportRuntimeHints 的 Registrar
 * 汇总成一份 RuntimeHints 交给构建插件——这就是"声明一次，构建期兑现"的挂钩点
 */
@Configuration
@ImportRuntimeHints(LegacyHintsRegistrar.class)
public class AotDemoConfig {

    @Bean
    public String aotDemoAnchor() {
        return "aot-demo";
    }
}
