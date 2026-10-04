package com.alec.InnovateX.spring.aop;

/**
 * 有接口的目标类（JDK 代理路线的靶子）。
 * pay() 走正常返回、refund() 走异常抛出，两条路径合起来把五种通知的精确顺序全部暴露给测试断言。
 */
public class AliPayChannel implements PaymentChannel {

    @Override
    public String pay(String orderNo, double amount) {
        System.out.println("[AliPayChannel] pay: orderNo=" + orderNo + ", amount=" + amount);
        return "pay:" + orderNo + ":" + amount;
    }

    @Override
    public String refund(String orderNo) {
        System.out.println("[AliPayChannel] refund: orderNo=" + orderNo);
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("退款单号不能为空");
        }
        return "refund:" + orderNo;
    }
}
