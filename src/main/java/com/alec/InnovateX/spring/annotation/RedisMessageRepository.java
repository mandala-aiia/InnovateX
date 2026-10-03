package com.alec.InnovateX.spring.annotation;

import org.springframework.stereotype.Repository;

/**
 * 普通实现：bean 名称默认为类名首字母小写 redisMessageRepository，
 * 注入点可用 @Qualifier("redisMessageRepository") 或 @Resource(name=...) 指定；
 * 另标注了自定义限定符 @Persistent——@Persistent 修饰的注入点会优先选中它（压过 @Primary）
 */
@Persistent
@Repository
public class RedisMessageRepository implements MessageRepository {

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public String save(String message) {
        return "[redis] 保存消息: " + message;
    }
}
