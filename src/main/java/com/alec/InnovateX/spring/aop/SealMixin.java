package com.alec.InnovateX.spring.aop;

/**
 * 引介的默认实现（mixin）：@DeclareParents 的 defaultImpl 指向它。
 * 代理对象会把 Sealable 接口的调用委托给一个"混入"的本实例——
 * 所以密封状态保存在 mixin 对象的字段上，而非目标对象的字段上（目标类根本没有这个字段）。
 */
public class SealMixin implements Sealable {

    private boolean sealed;

    @Override
    public void seal() {
        this.sealed = true;
        System.out.println("[SealMixin] 合同已密封");
    }

    @Override
    public void unseal() {
        this.sealed = false;
        System.out.println("[SealMixin] 合同解除密封");
    }

    @Override
    public boolean isSealed() {
        return sealed;
    }
}
