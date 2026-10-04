package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 构造器注入 + 两种"点名"方式（推荐构造器注入：字段可 final、依赖显式、便于单测）：
 * - @Qualifier("smsNotificationChannel")：按 bean 名称点名，绕过 @Primary
 * - @Durable（自定义元限定符）：按"语义标签"点名，同样绕过 @Primary
 * 两处注入的都是非首选实现，验证"限定符的优先级高于 @Primary"
 */
@Service
public class PinnedInjectionService {

    private final NotificationChannel pinnedByName;
    private final AuditStorage pinnedByQualifier;

    public PinnedInjectionService(@Qualifier("smsNotificationChannel") NotificationChannel pinnedByName,
                                  @Durable AuditStorage pinnedByQualifier) {
        this.pinnedByName = pinnedByName;
        this.pinnedByQualifier = pinnedByQualifier;
    }

    public String getChannelName() {
        return pinnedByName.name();
    }

    public String getStorageEngine() {
        return pinnedByQualifier.engine();
    }

    public String notifyUser(String user) {
        return pinnedByName.send(user, "您的订单已发货");
    }

    public String audit(String record) {
        return pinnedByQualifier.write(record);
    }
}
