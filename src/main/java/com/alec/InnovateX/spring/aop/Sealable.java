package com.alec.InnovateX.spring.aop;

/**
 * 引介增强（Introduction）要"凭空加给"目标对象的新接口。
 * 引介是五种通知之外的第六种增强：前五种织在"方法调用"上，引介直接改变"代理实现的类型集合"。
 * 目标类 ContractPaperService 不实现它、也不感知它的存在。
 */
public interface Sealable {

    void seal();

    void unseal();

    boolean isSealed();
}
