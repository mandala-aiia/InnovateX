package com.alec.InnovateX.spring.scope;

/**
 * 单例持有原型（陷阱现场）：构造单例 boss 时注入的那个 prototype 被永久「冻结」，
 * 之后无论调用多少次 heldProductId() 拿到的都是同一个实例。
 */
public class SingletonBoss {

    private final PrototypeProduct product;

    public SingletonBoss(PrototypeProduct product) {
        this.product = product;
    }

    public int heldProductId() {
        return product.id();
    }
}
