package com.alec.InnovateX.spring.event;

/** 监听器排序演示用事件：sku + 变动数量 */
public class StockMovedEvent {

    private final String sku;
    private final int delta;

    public StockMovedEvent(String sku, int delta) {
        this.sku = sku;
        this.delta = delta;
    }

    public String getSku() {
        return sku;
    }

    public int getDelta() {
        return delta;
    }
}
