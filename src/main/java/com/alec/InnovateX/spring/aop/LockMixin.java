package com.alec.InnovateX.spring.aop;

/**
 * 引介的默认实现（mixin）：@DeclareParents 的 defaultImpl 指向它。
 * 代理对象会把调用委托给一个"混入"的本实例——所以锁定状态保存在 mixin 对象上，而非目标对象
 */
public class LockMixin implements Lockable {

    private boolean locked;

    @Override
    public void lock() {
        this.locked = true;
        System.out.println("[LockMixin] 文档已锁定");
    }

    @Override
    public void unlock() {
        this.locked = false;
        System.out.println("[LockMixin] 文档已解锁");
    }

    @Override
    public boolean isLocked() {
        return locked;
    }
}
