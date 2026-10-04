package com.alec.InnovateX.spring.di;

import org.springframework.core.annotation.Order;

/** 短信渠道：@Order(1) → 注入 List 时排最前。 */
@Order(1)
public class SmsSender implements NotificationSender {

    @Override
    public String channel() {
        return "sms";
    }
}
