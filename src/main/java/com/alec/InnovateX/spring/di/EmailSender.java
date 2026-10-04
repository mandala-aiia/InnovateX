package com.alec.InnovateX.spring.di;

import org.springframework.core.annotation.Order;

/** 邮件渠道：@Order(2) 决定注入 List 时的排序位置。 */
@Order(2)
public class EmailSender implements NotificationSender {

    @Override
    public String channel() {
        return "email";
    }
}
