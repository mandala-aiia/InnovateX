package com.alec.InnovateX.spring.aware;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringValueResolver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Aware 六件套"接收器"：一个 Bean 同时实现六个基础设施注入接口，容器在初始化阶段
 * 逐个回调 setXxx，把底层能力"塞进来"——这是依赖注入的另一种形态：
 * 不主动找容器要，而是被动等容器给（回调注入 vs 依赖注入的对比点）。
 *
 * 回调顺序是确定的，且分两段来源：
 * 1) BeanFactoryAware 最早：由 AbstractAutowireCapableBeanFactory.invokeAwareMethods
 *    在进入 BeanPostProcessor 前置循环之前直接回调；
 * 2) 其余五个由 ApplicationContextAwareProcessor（容器内置的一个 BeanPostProcessor）
 *    在 invokeAwareInterfaces 中按固定代码顺序回调：
 *    Environment → EmbeddedValueResolver → ResourceLoader → ApplicationEventPublisher → MessageSource。
 *
 * 全部 Aware 回调完成之后，才轮到 @PostConstruct（由 CommonAnnotationBeanPostProcessor 处理，
 * 它注册在 ApplicationContextAwareProcessor 之后）——测试据此断言完整 7 步顺序。
 */
public class AwareSinkBean implements BeanFactoryAware, EnvironmentAware, EmbeddedValueResolverAware,
        ResourceLoaderAware, ApplicationEventPublisherAware, MessageSourceAware {

    /** 回调轨迹：供测试精确断言顺序（CopyOnWriteArrayList 保证教学演示下的写入安全） */
    public static final List<String> CALLBACK_TRACE = new CopyOnWriteArrayList<>();

    private BeanFactory beanFactory;

    private Environment environment;

    private StringValueResolver valueResolver;

    private ResourceLoader resourceLoader;

    private ApplicationEventPublisher eventPublisher;

    private MessageSource messageSource;

    /** 拿到底层 BeanFactory：比 ApplicationContext 更"低"的一层，可走 getBean 等底层 API */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        CALLBACK_TRACE.add("BeanFactoryAware");
        this.beanFactory = beanFactory;
        System.out.println("[AwareSinkBean] BeanFactoryAware 回调：拿到底层 BeanFactory");
    }

    /** 拿到 Environment：profile 与多 PropertySource（系统属性/环境变量/自定义）的统一视图 */
    @Override
    public void setEnvironment(Environment environment) {
        CALLBACK_TRACE.add("EnvironmentAware");
        this.environment = environment;
        System.out.println("[AwareSinkBean] EnvironmentAware 回调：拿到 Environment");
    }

    /** 拿到内嵌值解析器：@Value("${...}") 的底层机制就是它，可手动解析占位符 */
    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        CALLBACK_TRACE.add("EmbeddedValueResolverAware");
        this.valueResolver = resolver;
        System.out.println("[AwareSinkBean] EmbeddedValueResolverAware 回调：拿到占位符解析器");
    }

    /** 拿到资源加载器：classpath:/ file:/ http:// 等资源定位能力 */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        CALLBACK_TRACE.add("ResourceLoaderAware");
        this.resourceLoader = resourceLoader;
        System.out.println("[AwareSinkBean] ResourceLoaderAware 回调：拿到资源加载器");
    }

    /** 拿到事件发布器：不依赖 ApplicationContext 类型也能发事件（面向接口解耦） */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        CALLBACK_TRACE.add("ApplicationEventPublisherAware");
        this.eventPublisher = applicationEventPublisher;
        System.out.println("[AwareSinkBean] ApplicationEventPublisherAware 回调：拿到事件发布器");
    }

    /** 拿到国际化消息源：MessageSource.getMessage(...，Locale) */
    @Override
    public void setMessageSource(MessageSource messageSource) {
        CALLBACK_TRACE.add("MessageSourceAware");
        this.messageSource = messageSource;
        System.out.println("[AwareSinkBean] MessageSourceAware 回调：拿到消息源");
    }

    /** 六件套全部回调完毕之后才执行——顺序对比的教学锚点 */
    @PostConstruct
    public void postConstructPhase() {
        CALLBACK_TRACE.add("@PostConstruct");
        System.out.println("[AwareSinkBean] @PostConstruct：此时六个基础设施对象已全部注入");
    }

    /** EnvironmentAware 的实战演示：读 JVM 系统属性（系统属性本身就是 Environment 的一个 PropertySource） */
    public String readSystemProperty(String key) {
        return environment.getProperty(key);
    }

    /** EmbeddedValueResolverAware 的实战演示：手动解析 ${} 占位符 */
    public String resolvePlaceholder(String rawText) {
        return valueResolver.resolveStringValue(rawText);
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public StringValueResolver getValueResolver() {
        return valueResolver;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public ApplicationEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }
}
