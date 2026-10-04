package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 自定义作用域——线程作用域：同一线程内名字对应的 Bean 是同一个实例，跨线程各自独立。
 * <p>
 * 实现 org.springframework.beans.factory.config.Scope 即可接入容器（内置的
 * request/session 作用域本质上也是这个接口的实现），三个核心方法：
 * <ul>
 *   <li>{@link #get}：先查本线程缓存，没有才调用 objectFactory 让容器现场创建——
 *       容器只把"创建"外包给工厂，"放哪、放多久"由作用域自己决定；</li>
 *   <li>{@link #remove}：销毁本线程的实例（容器 destroyScopedBean 时回调）；</li>
 *   <li>{@link #registerDestructionCallback}：容器把"销毁逻辑"（含 @PreDestroy）注册进来，
 *       由作用域决定在什么时机触发——本实现选择在 remove() 时触发，正好闭合生命周期。</li>
 * </ul>
 * 注：Scope 接口的 resolveContextualObject/getConversationId 在 Spring 7 已给出 default 实现，
 * 这里仍覆写 getConversationId 展示"会话标识"概念（本实现即线程名）。
 */
public class ThreadScope implements Scope {

    public static final String SCOPE_NAME = "thread";

    /** 演示状态：观察销毁回调被注册/执行的历史（并发安全容器） */
    public static final Map<String, String> EVENTS = new ConcurrentHashMap<>();

    /** 每个线程一张"实例表"：key=beanName, value=该线程专属实例 */
    private final ThreadLocal<Map<String, Object>> threadObjects = ThreadLocal.withInitial(ConcurrentHashMap::new);

    /** 每个线程一张"销毁回调表"：key=beanName, value=容器注册的销毁逻辑 */
    private final ThreadLocal<Map<String, Runnable>> destructionCallbacks = ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> objects = threadObjects.get();
        Object bean = objects.get(name);
        if (bean == null) {
            // 缓存未命中：让容器工厂现场创建一个，随后缓存在"当前线程"的表里
            bean = objectFactory.getObject();
            objects.put(name, bean);
            EVENTS.put(name + ":created", Thread.currentThread().getName());
            System.out.println("[ThreadScope] 线程 " + Thread.currentThread().getName()
                    + " 首次创建 " + name + " 并缓存到线程本地");
        }
        return bean;
    }

    @Override
    public Object remove(String name) {
        destructionCallbacks.get().remove(name);
        Object removed = threadObjects.get().remove(name);
        if (removed != null) {
            EVENTS.put(name + ":removed", Thread.currentThread().getName());
            System.out.println("[ThreadScope] 线程 " + Thread.currentThread().getName() + " 移除 " + name);
        }
        return removed;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        destructionCallbacks.get().put(name, callback);
        EVENTS.put(name + ":callbackRegistered", Thread.currentThread().getName());
        System.out.println("[ThreadScope] 容器为 " + name + " 注册销毁回调（挂在线程 "
                + Thread.currentThread().getName() + " 上）");
    }

    @Override
    public Object resolveContextualObject(String key) {
        // 面向 request/session 这类"上下文作用域"的扩展点（如解析出当前 Request 对象），线程作用域用不到
        return null;
    }

    @Override
    public String getConversationId() {
        // 会话标识：线程作用域的"一次会话"就是当前线程
        return Thread.currentThread().getName();
    }
}
