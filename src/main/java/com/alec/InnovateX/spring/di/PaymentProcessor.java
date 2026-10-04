package com.alec.InnovateX.spring.di;

/** 支付渠道接口：两个实现用于演示 @Primary / @Qualifier / @Resource 的多候选消歧。 */
public interface PaymentProcessor {

    String pay(int cents);
}
