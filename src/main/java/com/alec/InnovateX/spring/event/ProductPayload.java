package com.alec.InnovateX.spring.event;

/**
 * 泛型事件载荷：商品（显式构造器 + final 字段 + 显式 getter，不用 lombok / record，保持教学风格统一）。
 */
public class ProductPayload {

    private final String sku;
    private final double price;

    public ProductPayload(String sku, double price) {
        this.sku = sku;
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public double getPrice() {
        return price;
    }
}
