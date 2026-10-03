package com.alec.InnovateX.spring.aop;

/** 引介增强要"凭空加给"目标对象的新接口——DocumentService 本身并不实现它 */
public interface Lockable {

    void lock();

    void unlock();

    boolean isLocked();
}
