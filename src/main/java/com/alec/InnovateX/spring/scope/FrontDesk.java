package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 单例持有 prototype 的经典矛盾与解法：
 * <p>
 * 矛盾——单例只注入一次，若直接注入 prototype Bean，它整个生命周期只用那一个实例，
 * prototype 的"每次新实例"语义在注入场景下失效。
 * <p>
 * 解法——给 prototype Bean 声明 proxyMode=TARGET_CLASS：注入进这里的是
 * <b>CGLIB 作用域代理</b>（字段类型是 ReceiptPrinter，实际对象是它的动态子类），
 * 代理把"每次调用 → 取新实例"的语义补了回来。等价于 XML 时代的 aop:scoped-proxy/。
 */
public class FrontDesk {

    /** 注入的是代理而非目标类——用 getter 让测试拿到它验证 AopUtils.isAopProxy */
    @Autowired
    private ReceiptPrinter receiptPrinter;

    public ReceiptPrinter getReceiptPrinter() {
        return receiptPrinter;
    }

    public String printFirst() {
        return receiptPrinter.print();
    }

    public String printSecond() {
        return receiptPrinter.print();
    }
}
