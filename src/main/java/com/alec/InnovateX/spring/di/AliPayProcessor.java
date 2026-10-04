package com.alec.InnovateX.spring.di;

/** 支付宝渠道。 */
public class AliPayProcessor implements PaymentProcessor {

    @Override
    public String pay(int cents) {
        return "支付宝支付:" + cents + "分";
    }
}
