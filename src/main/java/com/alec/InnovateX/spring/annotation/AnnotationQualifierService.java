package com.alec.InnovateX.spring.annotation;

import org.springframework.stereotype.Service;

/**
 * 语义化限定符注入：注入点用 @Persistent（自定义 @Qualifier 元注解）指定实现，
 * 会选中同样标注 @Persistent 的 RedisMessageRepository——即使 @Primary 在 Mysql 实现上
 */
@Service
public class AnnotationQualifierService {

    private final MessageRepository messageRepository;

    public AnnotationQualifierService(@Persistent MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public String repositoryType() {
        return messageRepository.type();
    }
}
