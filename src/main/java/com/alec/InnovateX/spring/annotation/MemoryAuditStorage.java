package com.alec.InnovateX.spring.annotation;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 内存审计存储：抽象类的 @Primary 首选实现——
 * 注入点既不点名也无限定符时，按类型注入多实现选它
 */
@Component
@Primary
@Order(1)
public class MemoryAuditStorage extends AuditStorage {

    @Override
    public String engine() {
        return "memory";
    }
}
