package com.alec.InnovateX.spring.annotation;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @Primary 标记为首选实现：当注入点没有 @Qualifier/@Resource 指定时，按类型注入多实现会选它
 */
@Repository
@Primary
public class MysqlMessageRepository implements MessageRepository {

    @Override
    public String type() {
        return "mysql";
    }

    @Override
    public String save(String message) {
        return "[mysql] 保存消息: " + message;
    }
}
