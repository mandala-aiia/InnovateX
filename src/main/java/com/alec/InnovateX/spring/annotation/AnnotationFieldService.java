package com.alec.InnovateX.spring.annotation;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字段注入两种方式的对比：
 * - @Resource 是 JSR-250 规范注解，默认按名称装配（byName）
 * - @Autowired 是 Spring 注解，默认按类型装配（byType）
 */
@Service
public class AnnotationFieldService {

    /** 按名称注入：显式指定 mysqlMessageRepository */
    @Resource(name = "mysqlMessageRepository")
    private MessageRepository byNameRepository;

    /** 按类型注入：多实现时依赖 @Primary 决定注入谁 */
    @Autowired
    private MessageRepository byTypeRepository;

    public String resourceType() {
        return byNameRepository.type();
    }

    public String autowiredType() {
        return byTypeRepository.type();
    }
}
