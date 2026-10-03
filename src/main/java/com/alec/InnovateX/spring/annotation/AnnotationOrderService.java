package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 构造器注入：注入点用 @Qualifier 指定非 @Primary 的实现
 * 注意：官方推荐构造器注入，便于不可变设计、完整初始化与单测
 */
@Service
public class AnnotationOrderService {

    private final MessageRepository messageRepository;

    // Lombok 的 @RequiredArgsConstructor 不会把 @Qualifier 复制到构造参数上，这里手写构造器
    public AnnotationOrderService(@Qualifier("redisMessageRepository") MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public String repositoryType() {
        return messageRepository.type();
    }

    public String createOrder(String orderNo) {
        return messageRepository.save("订单[" + orderNo + "]已创建");
    }
}
