package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.javaconfig.AppInfo;
import com.alec.InnovateX.spring.javaconfig.CustomerService;
import com.alec.InnovateX.spring.javaconfig.DeployService;
import com.alec.InnovateX.spring.javaconfig.DeferredBean;
import com.alec.InnovateX.spring.javaconfig.FeatureToggleConfig;
import com.alec.InnovateX.spring.javaconfig.FullProxyConfig;
import com.alec.InnovateX.spring.javaconfig.HealthProbe;
import com.alec.InnovateX.spring.javaconfig.ImportShowcaseConfig;
import com.alec.InnovateX.spring.javaconfig.LitePlainConfig;
import com.alec.InnovateX.spring.javaconfig.NetworkInfraBean;
import com.alec.InnovateX.spring.javaconfig.OrderController;
import com.alec.InnovateX.spring.javaconfig.PlainImportedBean;
import com.alec.InnovateX.spring.javaconfig.ProfileSwitchConfig;
import com.alec.InnovateX.spring.javaconfig.RecordBindingConfig;
import com.alec.InnovateX.spring.javaconfig.RegistrarCraftedBean;
import com.alec.InnovateX.spring.javaconfig.RegistryProcessorConfig;
import com.alec.InnovateX.spring.javaconfig.RootContextConfig;
import com.alec.InnovateX.spring.javaconfig.RuntimeGiftBean;
import com.alec.InnovateX.spring.javaconfig.SelectorPickedBean;
import com.alec.InnovateX.spring.javaconfig.ServerEndpoint;
import com.alec.InnovateX.spring.javaconfig.SharedComponent;
import com.alec.InnovateX.spring.javaconfig.StartupOrderConfig;
import com.alec.InnovateX.spring.javaconfig.StorageClient;
import com.alec.InnovateX.spring.javaconfig.StorageEngine;
import com.alec.InnovateX.spring.javaconfig.ToggleGuardedBean;
import com.alec.InnovateX.spring.javaconfig.WebLayerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题②Java Config 与模块化装配：full/lite 模式、@Import 三件套、@Conditional+ConditionContext、
 * @Profile、BeanDefinitionRegistryPostProcessor、@DependsOn、@Lazy、父子容器（委托+同名遮蔽）。
 * 每个 @Test 聚焦一个知识点
 */
public class JavaConfigTest {

    /** full 模式：@Bean 方法互调被 CGLIB 拦截，全程同一个单例，方法体只执行一次 */
    @Test
    public void fullModeProxyKeepsSingleton() {
        StorageEngine.CREATION_LOG.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(FullProxyConfig.class)) {
            FullProxyConfig config = ctx.getBean(FullProxyConfig.class);
            StorageEngine inContainer = ctx.getBean(StorageEngine.class);
            // 直接调配置类的 @Bean 方法也被代理拦截：返回容器里的同一个单例，方法体不会重复执行
            assertSame(inContainer, config.storageEngine());
            // storageClient() 内部对 storageEngine() 的调用同样被拦截
            assertSame(inContainer, ctx.getBean(StorageClient.class).getEngine());
            // 方法体自始至终只真实执行了一次
            assertEquals(1, StorageEngine.CREATION_LOG.size());
            System.out.println("full 模式创建记录: " + StorageEngine.CREATION_LOG);
        }
    }

    /** lite 模式：@Bean 方法互调是普通方法调用，每次执行都 new 新实例 */
    @Test
    public void liteModeEveryCallCreatesNewInstance() {
        StorageEngine.CREATION_LOG.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LitePlainConfig.class)) {
            LitePlainConfig config = ctx.getBean(LitePlainConfig.class);
            StorageEngine inContainer = ctx.getBean(StorageEngine.class);
            StorageEngine directCall = config.storageEngine();
            assertNotSame(inContainer, directCall);
            assertNotSame(inContainer, ctx.getBean(StorageClient.class).getEngine());
            // 容器 1 次 + storageClient() 方法内直调 1 次 + 测试直调 1 次 = 3 次
            assertEquals(3, StorageEngine.CREATION_LOG.size());
            System.out.println("lite 模式创建记录: " + StorageEngine.CREATION_LOG);
        }
    }

    /** @Import 三件套：普通类（bean 名=全限定类名）/ ImportSelector / ImportBeanDefinitionRegistrar */
    @Test
    public void importThreeStrategies() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ImportShowcaseConfig.class)) {
            // 1) 普通类导入：bean 名默认是全限定类名
            assertTrue(ctx.containsBean(PlainImportedBean.class.getName()));
            System.out.println("普通类导入的 bean 名: " + PlainImportedBean.class.getName());
            System.out.println(ctx.getBean(PlainImportedBean.class).describe());
            // 2) ImportSelector 选中：按类型断言（bean 名由 BeanNameGenerator 决定）
            assertEquals(1, ctx.getBeanNamesForType(SelectorPickedBean.class).length);
            System.out.println("selectorPickedBean 的 bean 名: "
                    + String.join(",", ctx.getBeanNamesForType(SelectorPickedBean.class)));
            System.out.println(ctx.getBean(SelectorPickedBean.class).describe());
            // 3) Registrar 手工注册：bean 名由注册代码自定义
            assertTrue(ctx.containsBean("registrarCraftedBean"));
            System.out.println(ctx.getBean(RegistrarCraftedBean.class).describe());
        }
    }

    /** 自定义 @Conditional + ConditionContext：Environment 属性开关决定 Bean 是否注册 */
    @Test
    public void conditionalOnEnvironmentProperty() {
        // 不提供开关属性：条件不成立，Bean 不注册
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(FeatureToggleConfig.class);
            ctx.refresh();
            assertFalse(ctx.containsBean("toggleGuardedBean"));
            System.out.println("开关属性未提供 => toggleGuardedBean 未注册");
        }
        // 用 MapPropertySource 提供开关（addFirst 头插 = 该属性源优先级最高），不污染全局系统属性
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("testFeatureToggle", Map.of("innovatex.feature.enabled", "true")));
            ctx.register(FeatureToggleConfig.class);
            ctx.refresh();
            assertTrue(ctx.containsBean("toggleGuardedBean"));
            System.out.println("开关属性已提供 => " + ctx.getBean(ToggleGuardedBean.class).describe());
        }
    }

    /** @Profile：同一接口的两个实现按激活的 profile 二选一注册 */
    @Test
    public void profileSelectsActiveEnvironment() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().setActiveProfiles("dev");
            ctx.register(ProfileSwitchConfig.class);
            ctx.refresh();
            assertEquals("dev", ctx.getBean(DeployService.class).environment());
            System.out.println("激活 dev profile => " + ctx.getBean(DeployService.class).environment());
        }
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().setActiveProfiles("prod");
            ctx.register(ProfileSwitchConfig.class);
            ctx.refresh();
            assertEquals("prod", ctx.getBean(DeployService.class).environment());
            System.out.println("激活 prod profile => " + ctx.getBean(DeployService.class).environment());
        }
    }

    /** BeanDefinitionRegistryPostProcessor：所有 Bean 实例化之前动态注册新 BeanDefinition */
    @Test
    public void registryPostProcessorRegistersDynamically() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(RegistryProcessorConfig.class)) {
            assertTrue(ctx.containsBean("runtimeGiftBean"));
            System.out.println(ctx.getBean(RuntimeGiftBean.class).describe());
        }
    }

    /** @DependsOn 强制初始化顺序（销毁自动反序）；@Lazy 推迟到首次 getBean 才实例化 */
    @Test
    public void dependsOnOrdersAndLazyDefers() {
        NetworkInfraBean.STARTUP_LOG.clear();
        DeferredBean.INSTANTIATED.set(false);
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(StartupOrderConfig.class)) {
            // 声明顺序 apiGateway 在前，@DependsOn 仍强制 networkInfra 先初始化
            assertTrue(NetworkInfraBean.STARTUP_LOG.indexOf("networkInfra") < NetworkInfraBean.STARTUP_LOG.indexOf("apiGateway"));
            System.out.println("初始化顺序: " + NetworkInfraBean.STARTUP_LOG);
            // @Lazy：BeanDefinition 已注册，但实例尚未创建
            assertTrue(ctx.containsBean("deferredBean"));
            assertFalse(DeferredBean.INSTANTIATED.get());
            ctx.getBean(DeferredBean.class);
            assertTrue(DeferredBean.INSTANTIATED.get());
            System.out.println("deferredBean 首次 getBean 后才实例化: " + ctx.getBean(DeferredBean.class).describe());
        }
    }

    /** 父子容器：子委托父取 Bean/解析依赖、层级单向可见、同名 Bean 互相遮蔽 */
    @Test
    public void parentChildContainerHierarchy() {
        // 父容器先就绪，子容器 setParent 后再 refresh（关闭顺序相反：子先关、父后关）
        try (GenericApplicationContext parent = new AnnotationConfigApplicationContext(RootContextConfig.class);
             AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext()) {
            child.setParent(parent);
            child.register(WebLayerConfig.class);
            child.refresh();

            // ① 子容器 getBean 找不到自己的定义时，向上委托父容器
            assertEquals("父容器", child.getBean("customerService", CustomerService.class).origin());
            System.out.println("子容器成功取到父容器 Bean: customerService");
            // ② 子容器 Bean 的依赖解析同样委托父容器（SSM 里 servlet 层注入 root 层服务的原理）
            assertEquals("子容器的 orderController 注入了来自父容器的 customerService",
                    child.getBean(OrderController.class).describe());
            System.out.println(child.getBean(OrderController.class).describe());
            // ③ 层级单向：父容器看不到子容器的 Bean
            assertThrows(NoSuchBeanDefinitionException.class, () -> parent.getBean("orderController"));
            System.out.println("父容器取 orderController => NoSuchBeanDefinitionException（单向可见）");
            // ④ 同名遮蔽：各自优先命中本容器的定义，互不影响
            assertEquals("子容器", child.getBean("messageBridge", SharedComponent.class).origin());
            assertEquals("父容器", parent.getBean("messageBridge", SharedComponent.class).origin());
            System.out.println("同名 messageBridge：子容器取到子容器版，父容器取到父容器版");
        }
    }

    /** record 作为 Bean：@Bean 返回 record、record 消费者单构造器隐式绑定、@Value 值固化进不可变载体 */
    @Test
    public void recordBeansWithImplicitConstructorBinding() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(RecordBindingConfig.class)) {
            // record 组件即只读属性（host()/port()），equals/hashCode/toString 自动获得
            ServerEndpoint endpoint = ctx.getBean(ServerEndpoint.class);
            assertEquals("innovatex.local", endpoint.host());
            assertEquals(8080, endpoint.port());

            // HealthProbe 也是 record：唯一规范构造器即注入点，没有 @Autowired 也没有 @Bean 参数手工传
            HealthProbe probe = ctx.getBean(HealthProbe.class);
            assertEquals(endpoint, probe.endpoint(), "注入的应是同一个 ServerEndpoint 单例");
            assertEquals("http://innovatex.local:8080/up", probe.report());

            // @Value 解析的占位符固化进 record：annotation-app.properties 里的 name/version
            AppInfo appInfo = ctx.getBean(AppInfo.class);
            assertEquals("InnovateX", appInfo.appName());
            assertEquals("1.0.0", appInfo.version());
            System.out.println("record Bean: " + endpoint + " / " + probe.report() + " / " + appInfo);
        }
    }
}
