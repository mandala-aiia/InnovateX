package com.alec.InnovateX.spring.ioc;

import java.util.concurrent.atomic.AtomicInteger;

/** 普通价计算器：原价返回。静态计数器用于观察「什么时候真正实例化」。 */
public class RegularPriceCalculator implements PriceCalculator {

    private static final AtomicInteger CREATED = new AtomicInteger();

    public RegularPriceCalculator() {
        CREATED.incrementAndGet();
    }

    public static int createdTotal() {
        return CREATED.get();
    }

    @Override
    public int price(int base) {
        return base;
    }
}
