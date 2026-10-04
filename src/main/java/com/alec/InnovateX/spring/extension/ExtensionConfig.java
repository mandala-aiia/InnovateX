package com.alec.InnovateX.spring.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 容器扩展点主题装配：FactoryBean 产品/本体两种获取方式 + 三类后处理器全部生效。
 */
@Configuration
public class ExtensionConfig {

    /** FactoryBean：注册的是"工厂"，按名取到的却是"产品" */
    @Bean
    public ReportDocumentFactoryBean reportDocumentFactory() {
        return new ReportDocumentFactoryBean();
    }

    /** BFPP 的改写目标：装配侧本来是急切实例化（未声明 lazy） */
    @Bean
    public OnDemandService onDemandService() {
        return new OnDemandService();
    }

    // ============================================================================
    // 后处理器必须用 static @Bean 注册（本主题的关键结论，写透原因）：
    //
    // BFPP/BPP 会被容器"提前"实例化——refresh 的 invokeBeanFactoryPostProcessors 与
    // registerBeanPostProcessors 阶段早于一切普通单例的创建。若用普通（非 static）@Bean 方法注册，
    // 容器必须先实例化本 @Configuration 类实例才能调用方法，这带来两个问题：
    // 1) 配置类（连同其构造器依赖）绕过这些后处理器被提前创建——本应被拦截/增强的对象漏网，
    //    破坏"后处理器先于业务 Bean 生效"的核心语义；
    // 2) 容器会打印 "bean ... is not eligible for getting processed by all BeanPostProcessors"
    //    警告，提示该 Bean 未享受到全部后处理器服务。
    // static 方法属于类而不属于实例：容器不必创建配置类对象即可调用它拿到后处理器实例，
    // 时序问题随之消失（@Bean 的 Javadoc 也明确给出同样建议）。
    // ============================================================================

    @Bean
    public static LazyFlipPostProcessor lazyFlipPostProcessor() {
        return new LazyFlipPostProcessor();
    }

    @Bean
    public static ConstructionAuditor constructionAuditor() {
        return new ConstructionAuditor();
    }

    @Bean
    public static InitializationAuditor initializationAuditor() {
        return new InitializationAuditor();
    }
}
