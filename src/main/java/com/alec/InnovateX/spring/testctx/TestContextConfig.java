package com.alec.InnovateX.spring.testctx;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * spring-test TestContext 框架演示装配（配套 SpringTestContextTest）：
 * 与手写 AnnotationConfigApplicationContext 的区别——上下文的生命周期交给
 * SpringExtension 管理，按 @ContextConfiguration 的"配置键"缓存复用；
 * @ActiveProfiles 在缓存键里，所以不同 profile 组合天然得到不同上下文
 */
@Configuration
public class TestContextConfig {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService();
    }

    @Bean
    public ContextCreationCounter contextCreationCounter() {
        return new ContextCreationCounter();
    }

    @Bean
    @Profile("!lab")
    public ModeReporter defaultModeReporter() {
        return new ModeReporter("default");
    }

    @Bean
    @Profile("lab")
    public ModeReporter labModeReporter() {
        return new ModeReporter("lab");
    }
}
