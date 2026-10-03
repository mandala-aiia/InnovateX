package com.alec.InnovateX.spring.scope;

import jakarta.annotation.PreDestroy;

/** prototype（原型作用域）：每次 getBean 都创建新实例，容器创建后交还调用方，不再管理其销毁 */
public class PrototypeBean {

    public static volatile int instanceCount = 0;

    public static volatile int destroyCount = 0;

    public PrototypeBean() {
        instanceCount++;
        System.out.println("[PrototypeBean] 创建实例 #" + instanceCount + " @" + Integer.toHexString(System.identityHashCode(this)));
    }

    @PreDestroy
    public void onDestroy() {
        destroyCount++;
        System.out.println("[PrototypeBean] @PreDestroy 被调用（prototype 理论上不会走到这里）");
    }
}
