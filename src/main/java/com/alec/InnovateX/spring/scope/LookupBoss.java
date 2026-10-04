package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Lookup;

/**
 * 解法三：方法注入。容器给 @Lookup 抽象方法生成 CGLIB 覆写实现，每次调用都做一次 getBean。
 * 注意：依赖 CGLIB 子类化，因此 bean 必须以「类」注册（@Component/registerBean），
 * 经工厂方法（@Bean）创建的实例不走这条路径、方法注入不生效。
 */
public abstract class LookupBoss {

    @Lookup
    public abstract PrototypeProduct createProduct();

    public int freshProductId() {
        return createProduct().id();
    }
}
