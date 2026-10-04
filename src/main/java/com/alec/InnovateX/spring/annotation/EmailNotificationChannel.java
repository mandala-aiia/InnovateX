package com.alec.InnovateX.spring.annotation;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 邮件渠道：
 * - @Primary：注入点未指明时，按类型注入多实现选它（"默认首选"）
 * - @Order(1)：控制集合注入（List/Map）与 ObjectProvider.orderedStream() 的排序，
 *   数字越小越靠前——否则集合顺序取决于 bean 注册顺序，演示与断言都不稳定
 */
@Component
@Primary
@Order(1)
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public String name() {
        return "email";
    }

    @Override
    public String send(String to, String content) {
        return "[EmailNotificationChannel] 发送邮件到 " + to + ": " + content;
    }
}
