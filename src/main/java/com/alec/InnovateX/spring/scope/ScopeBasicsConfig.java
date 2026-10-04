package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * 作用域基础对比配置：singleton / prototype / prototype+作用域代理。
 * <p>
 * 教学要点：
 * <ul>
 *   <li>singleton 是默认作用域，不写 @Scope 就是它；</li>
 *   <li>prototype 用 @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) 声明（常量即 "prototype"）；</li>
 *   <li>proxyMode 只对"非单例作用域 + 被单例注入"的场景有意义，TARGET_CLASS 表示用 CGLIB 子类代理
 *       （目标类没有接口时只能选它；等价于 XML 的 aop:scoped-proxy/）。</li>
 * </ul>
 * 故意不开启 @ComponentScan：同包还有多个演示配置类，扫描会互相干扰，全部用显式 @Bean 装配。
 */
@Configuration
public class ScopeBasicsConfig {

    /** 单例：容器级唯一，默认预实例化（refresh 阶段就创建） */
    @Bean
    public TicketMachine ticketMachine() {
        return new TicketMachine();
    }

    /** 原型：每次 getBean 新建；注意 @Bean 方法参数注入的依赖同样走容器解析 */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Ticket ticket(TicketMachine machine) {
        return new Ticket(machine.nextNumber());
    }

    /**
     * 原型 + 作用域代理：注入方拿到的是代理，方法调用时才取新实例。
     * 对比：如果去掉 proxyMode，FrontDesk 里注入的将是一个"固定"的 prototype 实例。
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ReceiptPrinter receiptPrinter() {
        return new ReceiptPrinter();
    }

    /** @Bean 创建的对象同样会被 AutowiredAnnotationBeanPostProcessor 处理字段注入 */
    @Bean
    public FrontDesk frontDesk() {
        return new FrontDesk();
    }
}
