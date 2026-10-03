package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义作用域——线程作用域：同一线程内 getBean 拿到同一实例，跨线程各自独立。
 * 实现 Scope 接口的五个方法即可，再通过 CustomScopeConfigurer 注册进容器。
 * Spring 内置的 request/session 作用域本质上也是 Scope 的实现（RequestScope/SessionScope）。
 */
public class ThreadScope implements Scope {

    public static final String SCOPE_NAME = "thread";

    private final ThreadLocal<Map<String, Object>> objects = ThreadLocal.withInitial(HashMap::new);

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> map = objects.get();
        Object bean = map.get(name);
        if (bean == null) {
            bean = objectFactory.getObject();
            map.put(name, bean);
            System.out.println("[ThreadScope] 线程 " + Thread.currentThread().getName() + " 首次创建: " + name);
        }
        return bean;
    }

    @Override
    public Object remove(String name) {
        return objects.get().remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        // 简化实现：线程结束难以可靠回调，真实实现（如 SessionScope）会挂在会话销毁时机上
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return Thread.currentThread().getName();
    }
}
