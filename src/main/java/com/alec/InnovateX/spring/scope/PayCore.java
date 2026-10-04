package com.alec.InnovateX.spring.scope;

/**
 * 构造器注入循环依赖的一端：构造时就要 RiskCore，而 RiskCore 构造时又要 PayCore。
 * <p>
 * 为什么无解：三级缓存的前提是"先实例化、后填属性"，这样才能把半成品先曝光出去；
 * 构造器注入连第一步实例化都无法完成（缺参数），根本没有机会进入缓存，
 * refresh 阶段即抛 BeanCurrentlyInCreationException。
 */
public class PayCore {

    private final RiskCore riskCore;

    public PayCore(RiskCore riskCore) {
        this.riskCore = riskCore;
        System.out.println("[PayCore] 构造完成（实际到不了这一行）");
    }

    public RiskCore getRiskCore() {
        return riskCore;
    }
}
