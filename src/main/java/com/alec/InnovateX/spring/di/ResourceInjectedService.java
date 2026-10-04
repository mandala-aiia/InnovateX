package com.alec.InnovateX.spring.di;

import jakarta.annotation.Resource;

/**
 * JSR-250 @Resource（jakarta.annotation）：
 * - 指定 name → 仅按名字找
 * - 不指定 → 先按字段名找 bean，找不到再回退按类型（此时同样遵守 @Primary）
 * 与 @Autowired（先按类型，再按名字）的查找顺序正好相反。
 */
public class ResourceInjectedService {

    @Resource(name = "weChatPayProcessor")
    private PaymentProcessor named;

    @Resource
    private PaymentProcessor aliPay;

    public String namedWay() {
        return named.pay(100);
    }

    public String defaultWay() {
        return aliPay.pay(100);
    }
}
