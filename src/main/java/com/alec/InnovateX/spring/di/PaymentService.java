package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 多候选消歧二重奏：
 * - 构造器参数用 @Qualifier("weChatPayProcessor") 点名要微信渠道
 * - setter 用普通 @Autowired → 走 @Primary 默认渠道（支付宝）
 */
public class PaymentService {

    private final PaymentProcessor chosen;

    private PaymentProcessor defaultOne;

    public PaymentService(@Qualifier("weChatPayProcessor") PaymentProcessor chosen) {
        this.chosen = chosen;
    }

    @Autowired
    public void setDefaultOne(PaymentProcessor defaultOne) {
        this.defaultOne = defaultOne;
    }

    public String chosenWay() {
        return chosen.pay(100);
    }

    public String defaultWay() {
        return defaultOne.pay(100);
    }
}
