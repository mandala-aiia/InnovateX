package com.alec.InnovateX.spring.annotation;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 短信渠道：bean 名默认 smsNotificationChannel（供 @Qualifier("smsNotificationChannel")
 * 或 @Resource(name=...) 精确引用）。
 * 另标注自定义限定符 @Durable：凡用 @Durable 修饰的注入点会精准命中它——优先级压过
 * Email 实现上的 @Primary
 */
@Component
@Durable
@Order(2)
public class SmsNotificationChannel implements NotificationChannel {

    @Override
    public String name() {
        return "sms";
    }

    @Override
    public String send(String to, String content) {
        return "[SmsNotificationChannel] 发送短信到 " + to + ": " + content;
    }
}
