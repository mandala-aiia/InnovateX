package com.alec.InnovateX.spring.di;

/** 微信渠道。 */
public class WeChatPayProcessor implements PaymentProcessor {

    @Override
    public String pay(int cents) {
        return "微信支付:" + cents + "分";
    }
}
