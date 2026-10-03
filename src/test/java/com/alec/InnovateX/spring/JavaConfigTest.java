package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.javaconfig.ChildConfig;
import com.alec.InnovateX.spring.javaconfig.ChildService;
import com.alec.InnovateX.spring.javaconfig.ConditionalBean;
import com.alec.InnovateX.spring.javaconfig.ConditionalConfig;
import com.alec.InnovateX.spring.javaconfig.DependsOnConfig;
import com.alec.InnovateX.spring.javaconfig.DependsOnFirst;
import com.alec.InnovateX.spring.javaconfig.FullModeConfig;
import com.alec.InnovateX.spring.javaconfig.ImportAggregateConfig;
import com.alec.InnovateX.spring.javaconfig.ImportedBean;
import com.alec.InnovateX.spring.javaconfig.LazyBean;
import com.alec.InnovateX.spring.javaconfig.LiteModeConfig;
import com.alec.InnovateX.spring.javaconfig.OrderDao;
import com.alec.InnovateX.spring.javaconfig.OrderDataSource;
import com.alec.InnovateX.spring.javaconfig.ParentConfig;
import com.alec.InnovateX.spring.javaconfig.ProfileConfig;
import com.alec.InnovateX.spring.javaconfig.ProfileService;
import com.alec.InnovateX.spring.javaconfig.RegistryConfig;
import com.alec.InnovateX.spring.javaconfig.RegistryDynamicBean;
import com.alec.InnovateX.spring.javaconfig.RegistrarImportedBean;
import com.alec.InnovateX.spring.javaconfig.SelectorImportedBean;
import com.alec.InnovateX.spring.javaconfig.SharedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题②Java Config 与模块装配：full/lite 模式、@Import 三件套、@Conditional、@Profile/Environment、
 * BeanDefinitionRegistryPostProcessor、@DependsOn、@Lazy
 */
public class JavaConfigTest {

    @Test
    public void fullModeCglibProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(FullModeConfig.class)) {
            FullModeConfig config = ctx.getBean(FullModeConfig.class);
            OrderDataSource dsInContainer = ctx.getBean(OrderDataSource.class);
            // full 模式下直接调用配置类的 @Bean 方法，被 CGLIB 拦截，返回容器中的同一个单例
            assertSame(dsInContainer, config.orderDataSource());
            // orderDao() 内部调用的 orderDataSource() 也被拦截，拿到的同样是容器单例
            assertSame(dsInContainer, ctx.getBean(OrderDao.class).getDataSource());
            System.out.println("full 模式：@Bean 方法互调被 CGLIB 拦截，全程同一个单例");
        }
    }

    @Test
    public void liteModeNoProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LiteModeConfig.class)) {
            LiteModeConfig config = ctx.getBean(LiteModeConfig.class);
            // lite 模式下 @Bean 方法互调就是普通方法调用，每次 new 新实例
            assertNotSame(ctx.getBean(OrderDataSource.class), config.orderDataSource());
            assertNotSame(ctx.getBean(OrderDataSource.class), ctx.getBean(OrderDao.class).getDataSource());
            System.out.println("lite 模式：@Bean 方法互调是普通调用，产生了不同实例");
        }
    }

    @Test
    public void importThreeWays() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ImportAggregateConfig.class)) {
            // 1) @Import 普通类：bean 名称是全限定类名
            assertTrue(ctx.containsBean(ImportedBean.class.getName()));
            System.out.println("普通类导入: " + ctx.getBean(ImportedBean.class).hello());
            // 2) ImportSelector 选中：按类型断言（bean 名称由 beanNameGenerator 决定）
            System.out.println("selectorImportedBean 的 bean 名称: "
                    + String.join(",", ctx.getBeanNamesForType(SelectorImportedBean.class)));
            assertEquals(1, ctx.getBeanNamesForType(SelectorImportedBean.class).length);
            System.out.println("Selector 导入: " + ctx.getBean(SelectorImportedBean.class).hello());
            // 3) ImportBeanDefinitionRegistrar 手动注册（bean 名称自定义）
            assertTrue(ctx.containsBean("registrarBean"));
            System.out.println("Registrar 注册: " + ctx.getBean(RegistrarImportedBean.class).hello());
        }
    }

    @Test
    public void conditionalAssembly() {
        // 未设置系统属性：条件不成立，Bean 不注册
        System.clearProperty("javaconfig.enabled");
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
            assertFalse(ctx.containsBean("conditionalBean"));
            System.out.println("javaconfig.enabled 未设置 => conditionalBean 未注册");
        }
        // 设置系统属性：条件成立，Bean 注册
        System.setProperty("javaconfig.enabled", "true");
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
            assertTrue(ctx.containsBean("conditionalBean"));
            System.out.println("javaconfig.enabled=true => " + ctx.getBean(ConditionalBean.class).hello());
        } finally {
            System.clearProperty("javaconfig.enabled");
        }
    }

    @Test
    public void profileAndEnvironment() {
        // dev profile：只有 DevProfileService 被注册
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().setActiveProfiles("dev");
            ctx.register(ProfileConfig.class);
            ctx.refresh();
            assertEquals("dev", ctx.getBean(ProfileService.class).env());
            System.out.println("激活 dev profile => " + ctx.getBean(ProfileService.class).env());
        }
        // prod profile：只有 ProdProfileService 被注册
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().setActiveProfiles("prod");
            ctx.register(ProfileConfig.class);
            ctx.refresh();
            assertEquals("prod", ctx.getBean(ProfileService.class).env());
        }
        // Environment 的 PropertySource 层级：后面的源先被遍历（优先级高）
        ConfigurableEnvironment env = new StandardEnvironment();
        System.out.println("PropertySource 优先级顺序（从高到低）: " + env.getPropertySources().stream().toList());
        env.getSystemProperties().put("demo.key", "systemProperties 中的值");
        System.out.println("getProperty('demo.key') = " + env.getProperty("demo.key"));
    }

    @Test
    public void registryPostProcessor() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(RegistryConfig.class)) {
            assertTrue(ctx.containsBean("registryDynamicBean"));
            System.out.println(ctx.getBean(RegistryDynamicBean.class).hello());
        }
    }

    @Test
    public void dependsOnAndLazy() {
        LazyBean.instantiated = false;
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DependsOnConfig.class)) {
            // @DependsOn 保证 dependsOnFirst 先于 dependsOnSecond 初始化
            assertTrue(DependsOnFirst.INIT_ORDER.indexOf("first") < DependsOnFirst.INIT_ORDER.indexOf("second"));
            System.out.println("初始化顺序: " + DependsOnFirst.INIT_ORDER);
            // @Lazy：容器启动完成后 lazyBean 仍未实例化
            assertTrue(ctx.containsBean("lazyBean"));
            assertFalse(LazyBean.instantiated);
            ctx.getBean(LazyBean.class);
            assertTrue(LazyBean.instantiated);
            System.out.println("@Lazy Bean 在第一次 getBean 时才实例化");
        }
    }

    @Test
    public void parentChildContainers() {
        // 父容器先启动，子容器 setParent 后再 refresh（关闭顺序反过来：子先关）
        try (GenericApplicationContext parent = new AnnotationConfigApplicationContext(ParentConfig.class);
             AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext()) {
            child.setParent(parent);
            child.register(ChildConfig.class);
            child.refresh();

            // 1) 子容器取不到自己的定义时，向上委托父容器
            assertEquals("父容器", child.getBean("parentOnlyService", SharedService.class).source());
            System.out.println("子容器成功取到父容器 Bean: parentOnlyService");

            // 2) 子容器的 Bean 依赖解析也会委托父容器（servlet 层注入 root 层服务的原理）
            System.out.println(child.getBean(ChildService.class).describe());
            assertEquals("子容器 Bean 注入了来自父容器的共享服务",
                    child.getBean(ChildService.class).describe());

            // 3) 反过来父容器看不到子容器的 Bean（层级是单向的）
            assertThrows(NoSuchBeanDefinitionException.class, () -> parent.getBean("childService"));
            System.out.println("父容器取 childService: NoSuchBeanDefinitionException（单向可见）");

            // 4) 同名遮蔽：子容器自己的定义优先，父容器不受影响
            assertEquals("子容器", child.getBean("configService", SharedService.class).source());
            assertEquals("父容器", parent.getBean("configService", SharedService.class).source());
            System.out.println("同名 bean: 子容器取到子容器版本，父容器取到父容器版本");
        }
    }
}
