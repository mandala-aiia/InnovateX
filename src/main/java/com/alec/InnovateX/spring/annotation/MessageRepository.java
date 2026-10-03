package com.alec.InnovateX.spring.annotation;

/**
 * 消息仓储接口：用于演示 @Autowired 按类型注入多实现时的歧义处理
 */
public interface MessageRepository {

    String type();

    String save(String message);
}
