package com.alec.InnovateX.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP 主装配：
 * - @EnableAspectJAutoProxy 注册 AnnotationAwareAspectJAutoProxyCreator——
 *   它是 BeanPostProcessor，在初始化后扫描容器里的 @Aspect Bean，给命中的目标 Bean 换成代理。
 * - exposeProxy = true：每次代理调用进入时把代理放进 AopContext 的 ThreadLocal，
 *   供 TransferTicketService 这类"目标类内部"取回代理修复自调用（有轻微开销，按需开启）。
 * - 全部用 @Bean 显式装配，不用 @ComponentScan：每个 Demo 由谁增强一目了然。
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopShowcaseConfig {

    // ---- 切面 ----

    @Bean
    public AuditTrailAspect auditTrailAspect() {
        return new AuditTrailAspect();
    }

    @Bean
    public PointcutSafariAspect pointcutSafariAspect() {
        return new PointcutSafariAspect();
    }

    @Bean
    public BookingAspect bookingAspect() {
        return new BookingAspect();
    }

    // ---- 目标对象 ----

    /** 返回类型声明为接口：有接口的目标默认走 JDK 动态代理 */
    @Bean
    public PaymentChannel paymentChannel() {
        return new AliPayChannel();
    }

    /** 无接口的目标：自动退回 CGLIB 子类代理 */
    @Bean
    public SmsNotifier smsNotifier() {
        return new SmsNotifier();
    }

    /** @Bean 方法参数注入：TradeDeskService 构造器注入 PaymentChannel 的代理 */
    @Bean
    public TradeDeskService tradeDeskService(PaymentChannel paymentChannel) {
        return new TradeDeskService(paymentChannel);
    }

    @Bean
    public TransferTicketService transferTicketService() {
        return new TransferTicketService();
    }
}
