package com.alec.InnovateX.spring.javaconfig;

import java.util.concurrent.atomic.AtomicBoolean;

/** @Lazy Bean：用静态开关记录实例化时刻，验证"启动不建、首次 getBean 才建" */
public class DeferredBean {

    public static final AtomicBoolean INSTANTIATED = new AtomicBoolean(false);

    public DeferredBean() {
        INSTANTIATED.set(true);
        System.out.println("[DeferredBean] 第一次 getBean 才会执行到这里");
    }

    public String describe() {
        return "我是延迟到第一次 getBean 才创建的 Bean";
    }
}
