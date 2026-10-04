package com.alec.InnovateX.spring.di;

import org.springframework.core.annotation.Order;

/** 推送渠道：@Order(3) → 注入 List 时排最后。 */
@Order(3)
public class PushSender implements NotificationSender {

    @Override
    public String channel() {
        return "push";
    }
}
