package com.alec.InnovateX.spring.scope;

/** singleton（默认作用域）：容器内只有一个实例，由容器负责创建与销毁 */
public class SingletonBean {

    public static volatile int instanceCount = 0;

    public SingletonBean() {
        instanceCount++;
        System.out.println("[SingletonBean] 创建实例 #" + instanceCount + " @" + Integer.toHexString(System.identityHashCode(this)));
    }
}
