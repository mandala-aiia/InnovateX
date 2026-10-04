package com.alec.InnovateX.spring.aop;

/**
 * 支付渠道接口——Spring AOP 两条代理路线的分水岭。
 * 目标类实现了接口：默认走 JDK 动态代理（运行期对"接口"生成 $Proxy 子类，目标类躲藏在代理背后）；
 * 目标类没有接口：自动退回 CGLIB（运行期对"类"生成子类）。
 * 本主题用它在 {@link AliPayChannel} 上演示 JDK 路线，用 {@link SmsNotifier} 演示 CGLIB 路线。
 */
public interface PaymentChannel {

    /** 正常路径：返回回执字符串，供 @AfterReturning 绑定返回值 */
    String pay(String orderNo, double amount);

    /** 异常路径：参数非法时抛 IllegalArgumentException，供 @AfterThrowing 捕获 */
    String refund(String orderNo);
}
