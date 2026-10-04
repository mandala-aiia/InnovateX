package com.alec.InnovateX.spring.scope;

/**
 * 构造器注入循环依赖的另一端：与 {@link PayCore} 互相构造依赖，形成死锁式的环。
 */
public class RiskCore {

    private final PayCore payCore;

    public RiskCore(PayCore payCore) {
        this.payCore = payCore;
        System.out.println("[RiskCore] 构造完成（实际到不了这一行）");
    }

    public PayCore getPayCore() {
        return payCore;
    }
}
