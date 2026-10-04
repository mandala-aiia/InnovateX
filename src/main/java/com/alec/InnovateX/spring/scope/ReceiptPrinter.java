package com.alec.InnovateX.spring.scope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作用域代理的"目标 Bean"：prototype 作用域 + proxyMode=TARGET_CLASS。
 *
 * 它自己平平无奇——关键在装配处（见 {@link ScopeBasicsConfig#receiptPrinter()}）：
 * 声明 proxyMode 后，注入到单例里的不是它本尊，而是 CGLIB 生成的作用域代理，
 * 每次调用代理方法时代理才去容器 getBean 拿一个全新 prototype 来执行。
 */
public class ReceiptPrinter {

    /** 演示状态：记录目标实例被真正创建的次数（每次代理调用 +1） */
    public static final AtomicInteger CREATED = new AtomicInteger();

    public ReceiptPrinter() {
        CREATED.incrementAndGet();
        System.out.println("[ReceiptPrinter] 目标实例创建（累计 " + CREATED.get() + "）@"
                + Integer.toHexString(System.identityHashCode(this)));
    }

    /** 打印小票并附带自己的身份，用来证明"每次调用都是新实例" */
    public String print() {
        return "小票来自 ReceiptPrinter@" + Integer.toHexString(System.identityHashCode(this));
    }
}
