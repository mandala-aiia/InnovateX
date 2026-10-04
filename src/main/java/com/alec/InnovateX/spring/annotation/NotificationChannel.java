package com.alec.InnovateX.spring.annotation;

/**
 * 通知渠道接口：刻意安排两个实现（Email/Sms），
 * 用于演示"按类型注入遇到多实现"的全套歧义处理——
 * @Primary、@Qualifier、@Resource、自定义限定符都会在这两个实现之间做选择
 */
public interface NotificationChannel {

    /** 渠道名：测试断言"注入了哪个实现"的依据 */
    String name();

    /** 业务方法：发送通知 */
    String send(String to, String content);
}
