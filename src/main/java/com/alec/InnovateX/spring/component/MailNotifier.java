package com.alec.InnovateX.spring.component;

/** 邮件通知器：普通类，经 @Bean 方法显式声明。 */
public class MailNotifier {

    public String send() {
        return "邮件已发送";
    }
}
