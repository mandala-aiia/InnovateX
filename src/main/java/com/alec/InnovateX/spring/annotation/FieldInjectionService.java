package com.alec.InnovateX.spring.annotation;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字段注入双注解对照（本类刻意用字段注入——知识点本身就在讲"注入点注解"；工程上更推荐构造器注入）：
 * - @Resource 是 JSR-250 标准注解：装配规则为"先按名称"；不指定 name 时先用字段名找，
 *   找不到 bean 再回退按类型
 * - @Autowired 是 Spring 注解：装配规则为"先按类型"，多个候选时依次参考 @Primary/@Qualifier/字段名
 */
@Service
public class FieldInjectionService {

    /** @Resource 显式指定 name：直取 smsNotificationChannel，无视 @Primary */
    @Resource(name = "smsNotificationChannel")
    private NotificationChannel smsByName;

    /** @Autowired 按类型：email/sms 两个候选 -> @Primary 的 email 胜出 */
    @Autowired
    private NotificationChannel channelByType;

    /** @Resource 未指定 name：先按字段名 "auditStorage" 找 bean（不存在）-> 回退按类型 -> @Primary 的 memory 胜出 */
    @Resource
    private AuditStorage auditStorage;

    public String getSmsByName() {
        return smsByName.name();
    }

    public String getChannelByType() {
        return channelByType.name();
    }

    public String getAuditStorageEngine() {
        return auditStorage.engine();
    }
}
