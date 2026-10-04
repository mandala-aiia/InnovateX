package com.alec.InnovateX.spring.ioc;

/**
 * 价格计算器接口：容器里会注册多个实现，用于演示按类型获取时的 NoUniqueBeanDefinitionException、
 * ObjectProvider 的 stream/orderedStream、getBeansOfType 等「多候选」场景。
 */
public interface PriceCalculator {

    int price(int base);
}
