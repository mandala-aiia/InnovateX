package com.alec.InnovateX.spring.lifecycle;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * SmartInitializingSingleton：所有"非懒加载"单例都完成实例化后回调一次
 * （对比 @PostConstruct 只代表"当前 Bean"就绪，它代表"全体就绪"）。
 * Spring 事件广播器初始化等就发生在这一时机之后，@EventListener 的注册也以此为界
 */
@Component
public class SmartSingletonHook implements SmartInitializingSingleton {

    private static volatile boolean invoked = false;

    private static volatile boolean plainBeanReadyWhenInvoked = false;

    @Override
    public void afterSingletonsInstantiated() {
        invoked = true;
        // 此回调触发时，容器里所有单例（包括 PlainSingletonBean）必然已初始化完毕
        plainBeanReadyWhenInvoked = PlainSingletonBean.isInitialized();
        System.out.println("[SmartSingletonHook] 所有单例初始化完成，PlainSingletonBean 已就绪=" + plainBeanReadyWhenInvoked);
    }

    public static boolean isInvoked() {
        return invoked;
    }

    public static boolean isPlainBeanReadyWhenInvoked() {
        return plainBeanReadyWhenInvoked;
    }
}
