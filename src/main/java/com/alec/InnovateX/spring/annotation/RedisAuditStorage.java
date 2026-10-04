package com.alec.InnovateX.spring.annotation;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Redis 审计存储：标注自定义限定符 @Durable——
 * 用 @Durable 修饰的注入点会选中它，即使 @Primary 在 Memory 实现上
 */
@Component
@Durable
@Order(2)
public class RedisAuditStorage extends AuditStorage {

    @Override
    public String engine() {
        return "redis";
    }
}
