package com.alec.InnovateX.spring.scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

/**
 * 自制的"迷你自动代理创建器"——精确复刻 AOP 基础设施（AbstractAutoProxyCreator）
 * 参与三级缓存的行为约定，两个回调各司其职：
 * <ul>
 *   <li>{@link #getEarlyBeanReference}：三级缓存的出口。仅当某个"正在创建中"的 Bean 被别人
 *       （循环依赖方）需要时，容器才从第三级缓存取出工厂调用它——在这里<b>提前</b>生成代理并缓存，
 *       让对方注入的一开始就是代理而不是原始对象；</li>
 *   <li>{@link #postProcessAfterInitialization}：正常的代理生成时机（Bean 初始化完成后）。
 *       关键纪律：若该 Bean 曾被提前代理，这里必须返回<b>原始对象</b>（AbstractAutoProxyCreator 同款约定），
 *       让容器在 doCreateBean 收尾时用"早期单例引用"统一替换成同一个代理——
 *       若在这里另造/复用代理返回，暴露对象与早期引用不一致，Spring 7 更严格的引导锁
 *       会判定引导不一致并重走创建流程（实测出现目标类被构造两次的浪费）。</li>
 * </ul>
 * 用 ConcurrentHashMap 记录"谁被早期代理过"保证幂等，用 CopyOnWriteArrayList 记录历史供测试断言。
 */
public class EarlyProxyProcessor implements SmartInstantiationAwareBeanPostProcessor {

    /** 观察点一：哪些 Bean 走过三级缓存的早期曝光 */
    public static final CopyOnWriteArrayList<String> EARLY_EXPOSED = new CopyOnWriteArrayList<>();

    /** 观察点二：代理拦截到了哪些方法调用 */
    public static final CopyOnWriteArrayList<String> INTERCEPTED = new CopyOnWriteArrayList<>();

    /** beanName → 已生成的代理，保证早期代理与最终代理是同一个对象 */
    private final Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        EARLY_EXPOSED.add(beanName);
        System.out.println("[三级缓存] getEarlyBeanReference 触发: " + beanName
                + "（此时尚未完成初始化，就被循环依赖方索要）");
        return wrapIfTarget(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (proxyCache.containsKey(beanName)) {
            // 曾在三级缓存里提前代理过：按 AbstractAutoProxyCreator 的约定返回原始对象，
            // 由容器用早期单例引用完成最终替换（保证"注入的代理 == 最终的代理"且只创建一次）
            System.out.println("[三级缓存] " + beanName + " 初始化完成；早期已生成过代理，交还原始对象由容器替换");
            return bean;
        }
        return wrapIfTarget(bean, beanName);
    }

    /** 只代理本演示的目标 Bean；其余 Bean 原样返回 */
    private Object wrapIfTarget(Object bean, String beanName) {
        if (!"alarmCenter".equals(beanName) || proxyCache.containsKey(beanName)) {
            return bean;
        }
        ProxyFactory factory = new ProxyFactory(bean);
        factory.setProxyTargetClass(true); // 无接口 → CGLIB 子类代理
        factory.addAdvice((MethodInterceptor) invocation -> {
            INTERCEPTED.add(invocation.getMethod().getName());
            System.out.println("[早期代理] 拦截方法: " + invocation.getMethod().getName());
            return invocation.proceed();
        });
        Object proxy = factory.getProxy();
        proxyCache.put(beanName, proxy);
        System.out.println("[三级缓存] 为 " + beanName + " 生成代理 @" + Integer.toHexString(System.identityHashCode(proxy)));
        return proxy;
    }
}
