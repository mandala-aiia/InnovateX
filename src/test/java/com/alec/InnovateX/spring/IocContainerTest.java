package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.ioc.GreetingCard;
import com.alec.InnovateX.spring.ioc.GreetingCardFactoryBean;
import com.alec.InnovateX.spring.ioc.Greeter;
import com.alec.InnovateX.spring.ioc.GreeterCustomizingBFPP;
import com.alec.InnovateX.spring.ioc.OrderRepository;
import com.alec.InnovateX.spring.ioc.OrderService;
import com.alec.InnovateX.spring.ioc.PriceCalculator;
import com.alec.InnovateX.spring.ioc.RegularPriceCalculator;
import com.alec.InnovateX.spring.ioc.ShopConfig;
import com.alec.InnovateX.spring.ioc.VipPriceCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring IoC 容器基础（core/bean 第一课）：
 * - AnnotationConfigApplicationContext 最小用法、XML 方式一瞥
 * - getBean 各重载与两大经典异常（NoSuchBeanDefinition / NoUniqueBeanDefinition）
 * - ObjectProvider：可选依赖的惰性句柄（getIfAvailable / ifAvailable / stream / orderedStream）
 * - ListableBeanFactory：getBeanNamesForType 只列举不实例化，getBeansOfType 才真正实例化
 * - 编程式注册：registerBean(Class, Supplier, Customizer) 与裸 BeanDefinitionBuilder
 * - FactoryBean（产品与 & 工厂）、别名、BFPP 在 ApplicationContext 与裸 BeanFactory 下的执行差异
 */
public class IocContainerTest {

    /** @Configuration + @Bean 的最小容器：getBean 按类型/名字拿到同一个单例。 */
    @Test
    public void minimalAnnotationConfigContext() {
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            assertEquals("订单数=42", ctx.getBean(OrderService.class).summary());
            assertSame(ctx.getBean(OrderService.class), ctx.getBean("orderService", OrderService.class));
            assertTrue(ctx.containsBean("orderRepository"));
            assertTrue(ctx.isSingleton("orderRepository"));
        }
    }

    /** getBean 的重载与缺 bean 异常：按类型/按名/按名+类型，取不到抛 NoSuchBeanDefinitionException。 */
    @Test
    public void getBeanOverloadsAndMissing() {
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            OrderService byType = ctx.getBean(OrderService.class);
            assertSame(byType, ctx.getBean("orderService"));
            assertSame(byType, ctx.getBean("orderService", OrderService.class));
            assertThrows(NoSuchBeanDefinitionException.class, () -> ctx.getBean("不存在的名字"));
            assertThrows(NoSuchBeanDefinitionException.class, () -> ctx.getBean(GreetingCard.class));
        }
    }

    /** 同一类型多个候选且无 @Primary/@Qualifier 时，按类型 getBean 抛 NoUniqueBeanDefinitionException；按名字可精确取。 */
    @Test
    public void noUniqueResolution() {
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            assertThrows(NoUniqueBeanDefinitionException.class, () -> ctx.getBean(PriceCalculator.class));
            PriceCalculator vip = assertInstanceOf(VipPriceCalculator.class, ctx.getBean("vipPriceCalculator"));
            assertEquals(90, vip.price(100));
        }
    }

    /** ObjectProvider：把「类型查找」变成惰性、可选、可遍历的句柄，getBeanProvider 本身不会实例化 bean。 */
    @Test
    public void objectProvider() {
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            ObjectProvider<OrderRepository> provider = ctx.getBeanProvider(OrderRepository.class);
            assertSame(ctx.getBean(OrderRepository.class), provider.getIfAvailable());

            AtomicBoolean invoked = new AtomicBoolean(false);
            provider.ifAvailable(repo -> invoked.set(true));
            assertTrue(invoked.get(), "存在时 ifAvailable 回调应执行");

            // 只匹配精确类型：VipPriceCalculator 是唯一实现，stream 只有一个元素
            assertEquals(1, ctx.getBeanProvider(VipPriceCalculator.class).stream().count());
            // 声明成接口类型则拿到全部实现，orderedStream 按 @Order/注册序排列
            assertEquals(2, ctx.getBeanProvider(PriceCalculator.class).orderedStream().count());

            ObjectProvider<GreetingCard> absent = ctx.getBeanProvider(GreetingCard.class);
            assertNull(absent.getIfAvailable());
            AtomicBoolean neverRun = new AtomicBoolean(false);
            absent.ifAvailable(card -> neverRun.set(true));
            assertTrue(!neverRun.get(), "不存在时 ifAvailable 回调不应执行");
            assertThrows(NoSuchBeanDefinitionException.class, absent::getObject);
        }
    }

    /** 列举 vs 实例化：getBeanNamesForType 只读 BeanDefinition 不创建实例，getBeansOfType 会真正调用工厂方法。 */
    @Test
    public void listableApiDoesNotInstantiateUntilAsked() {
        int before = RegularPriceCalculator.createdTotal() + VipPriceCalculator.createdTotal();
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            // 两个计算器是 @Lazy：refresh 结束后仍未实例化
            assertEquals(before, RegularPriceCalculator.createdTotal() + VipPriceCalculator.createdTotal());

            List<String> names = Arrays.asList(ctx.getBeanNamesForType(PriceCalculator.class));
            assertEquals(List.of("regularPriceCalculator", "vipPriceCalculator"), names);
            // 列举名字依旧不触发实例化
            assertEquals(before, RegularPriceCalculator.createdTotal() + VipPriceCalculator.createdTotal());

            ctx.getBeansOfType(PriceCalculator.class);
            // getBeansOfType 真正实例化了两个 bean
            assertEquals(before + 2, RegularPriceCalculator.createdTotal() + VipPriceCalculator.createdTotal());
        }
    }

    /** 编程式注册：registerBean + Supplier 创建 + BeanDefinitionCustomizer 微调定义（setPrimary、属性、作用域等）。 */
    @Test
    public void programmaticRegisterBean() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(OrderRepository.class);
            ctx.registerBean("vipCalculator", VipPriceCalculator.class, VipPriceCalculator::new,
                    bd -> bd.setPrimary(true));
            ctx.registerBean(RegularPriceCalculator.class, RegularPriceCalculator::new);
            // 普通类注册：唯一构造器的参数会自动从容器按类型解析
            ctx.registerBean(OrderService.class);
            ctx.refresh();

            assertEquals("默认构造", ctx.getBean(OrderRepository.class).getSource());
            // 同类型两个候选，但 vipCalculator 被标记 @Primary → 按类型获取不再抛 NoUnique
            assertInstanceOf(VipPriceCalculator.class, ctx.getBean(PriceCalculator.class));
            assertEquals("订单数=42", ctx.getBean(OrderService.class).summary());
        }
    }

    /** 裸 DefaultListableBeanFactory + BeanDefinitionBuilder：绕过注解，直接拼 BeanDefinition（构造参数、属性、引用）。 */
    @Test
    public void beanDefinitionBuilderApi() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("repo", BeanDefinitionBuilder
                .genericBeanDefinition(OrderRepository.class)
                .addPropertyValue("source", "BeanDefinitionBuilder 注册")
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("orderService", BeanDefinitionBuilder
                .genericBeanDefinition(OrderService.class)
                .addConstructorArgReference("repo")
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("greeter", BeanDefinitionBuilder
                .genericBeanDefinition(Greeter.class)
                .getBeanDefinition());

        // getBean 传构造参数：仅在「创建新实例」时生效，之后命中单例缓存直接复用
        Greeter byArgs = beanFactory.getBean(Greeter.class, "运行期构造参数");
        assertEquals("运行期构造参数", byArgs.getMessage());
        Greeter cached = beanFactory.getBean("greeter", Greeter.class);
        assertSame(byArgs, cached, "再次 getBean 命中缓存，构造参数被忽略");

        // 手动 getBeanNamesForType? 不需要 —— 直接验证按定义注册的引用与属性注入
        OrderService service = beanFactory.getBean("orderService", OrderService.class);
        assertEquals("订单数=42", service.summary());
        assertEquals("BeanDefinitionBuilder 注册", service.getOrderRepository().getSource());
    }

    /** BFPP 执行差异：ApplicationContext 在 refresh 时自动执行 BFPP；裸 BeanFactory 必须手动调用。 */
    @Test
    public void beanFactoryPostProcessorExecutionDifference() {
        // 1) ApplicationContext：BFPP 注册为 bean 后自动执行（先于所有单例实例化）
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(Greeter.class, () -> new Greeter("原始消息"));
            ctx.registerBean(GreeterCustomizingBFPP.class);
            ctx.refresh();
            assertEquals("被 BFPP 修改过的问候", ctx.getBean(Greeter.class).getMessage());
        }

        // 2) 裸 DefaultListableBeanFactory：不会自动执行任何 BFPP
        DefaultListableBeanFactory noBfpp = new DefaultListableBeanFactory();
        noBfpp.registerBeanDefinition("greeter", greeterDefinition());
        assertEquals("裸工厂原始消息", noBfpp.getBean("greeter", Greeter.class).getMessage());

        // 3) 手动执行 BFPP：必须发生在任何 bean 创建之前
        //    （BFPP 改的是 BeanDefinition；一旦 getBean 过，合并定义已被缓存，改原始定义也来不及了）
        DefaultListableBeanFactory manualBfpp = new DefaultListableBeanFactory();
        manualBfpp.registerBeanDefinition("greeter", greeterDefinition());
        new GreeterCustomizingBFPP().postProcessBeanFactory(manualBfpp);
        Greeter modified = manualBfpp.getBean("greeter", Greeter.class);
        // 构造参数先赋值，BFPP 追加的 message 属性后注入 → 属性胜出
        assertEquals("被 BFPP 修改过的问候", modified.getMessage());
    }

    private static org.springframework.beans.factory.config.BeanDefinition greeterDefinition() {
        return BeanDefinitionBuilder.genericBeanDefinition(Greeter.class)
                .addConstructorArgValue("裸工厂原始消息")
                .getBeanDefinition();
    }

    /** FactoryBean：容器里注册的是工厂，getBean 拿产品，& 前缀拿工厂本身；isSingleton=true 时产品被缓存。 */
    @Test
    public void factoryBeanProductAndItself() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean("greetingCard", GreetingCardFactoryBean.class);
            ctx.refresh();

            GreetingCard card = assertInstanceOf(GreetingCard.class, ctx.getBean("greetingCard"));
            assertEquals("你好, Spring 7", card.getMessage());
            assertSame(card, ctx.getBean("greetingCard", GreetingCard.class));
            assertSame(card, ctx.getBean(GreetingCard.class));
            // & 前缀：拿到的是工厂本身而不是产品
            assertInstanceOf(GreetingCardFactoryBean.class, ctx.getBean("&greetingCard"));
        }
    }

    /** 别名：registerAlias 为既有 bean 名再挂名字，getBean/按类型查找都能命中同一实例。 */
    @Test
    public void aliasRegistration() {
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            var beanFactory = ctx.getBeanFactory();
            beanFactory.registerAlias("orderService", "svcAlias");
            beanFactory.registerAlias("orderService", "orderSvc");

            assertSame(ctx.getBean("orderService"), ctx.getBean("svcAlias"));
            assertTrue(Arrays.asList(ctx.getAliases("orderService")).containsAll(List.of("svcAlias", "orderSvc")));
            assertInstanceOf(OrderService.class, ctx.getBean("orderSvc", OrderService.class));
        }
    }

    /** XML 容器一瞥：ClassPathXmlApplicationContext 加载 classpath 下的 XML，构造参数与属性同样生效。 */
    @Test
    public void xmlContext() {
        try (var ctx = new ClassPathXmlApplicationContext("ioc/context.xml")) {
            assertEquals("订单数=42", ctx.getBean(OrderService.class).summary());
            assertEquals("来自 XML 的问候", ctx.getBean(Greeter.class).getMessage());
            assertSame(ctx.getBean("orderService"), ctx.getBean("orderService", OrderService.class));
        }
    }
}
