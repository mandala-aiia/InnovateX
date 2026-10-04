package com.alec.InnovateX.spring.ioc;

import java.util.concurrent.atomic.AtomicInteger;

/** VIP 价计算器：九折。静态计数器用于观察「什么时候真正实例化」。 */
public class VipPriceCalculator implements PriceCalculator {

    private static final AtomicInteger CREATED = new AtomicInteger();

    public VipPriceCalculator() {
        CREATED.incrementAndGet();
    }

    public static int createdTotal() {
        return CREATED.get();
    }

    @Override
    public int price(int base) {
        return base * 9 / 10;
    }
}
