package com.alec.InnovateX.spring.aop;

/**
 * 切点动物园的靶子类，兼任"业务类显式构造器注入"示范：
 * final 字段 + 显式构造器 + 显式 getter（不依赖 lombok）。
 *
 * settle() 演示"跨 Bean 调用"：this.paymentChannel 是容器注入的"代理对象"，
 * 调用它的方法天然经过拦截器链——这正是与 TransferTicketService 自调用失效场景的对照。
 */
public class TradeDeskService {

    private final PaymentChannel paymentChannel;

    public TradeDeskService(PaymentChannel paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public PaymentChannel getPaymentChannel() {
        return paymentChannel;
    }

    /** 跨 Bean 委托：调的是 PaymentChannel 的 JDK 代理，支付切面照常生效 */
    public String settle(String orderNo) {
        String receipt = paymentChannel.pay(orderNo, 1.0);
        System.out.println("[TradeDeskService] settle: " + receipt);
        return receipt;
    }

    // ---- 以下方法分别被不同维度的切点命中 ----

    public String withinDemo() {
        return "withinDemo";
    }

    public String targetDemo() {
        return "targetDemo";
    }

    /** 签名恰好 (String, Integer)，被 args(String, Integer) 命中 */
    public String argsDemo(String sku, Integer quantity) {
        return "argsDemo:" + sku + "/" + quantity;
    }

    public String beanDemo() {
        return "beanDemo";
    }

    /** 方法上标注自定义注解，被 @annotation(audited) 命中且注解本体被绑定 */
    @Audited(tag = "desk-audit")
    public String annotationDemo() {
        return "annotationDemo";
    }
}
