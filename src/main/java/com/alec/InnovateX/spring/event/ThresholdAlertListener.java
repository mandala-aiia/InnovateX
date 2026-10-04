package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 条件监听器：@EventListener 的 condition 是一段 SpEL，#event 绑定当前事件，
 * 表达式为 false 时监听器被整体跳过（连方法体都不进）——
 * 相当于把 if 过滤从每个监听器里提炼到了声明处
 */
public class ThresholdAlertListener {

    public static final List<String> ALERTS = new CopyOnWriteArrayList<>();

    /** 只关心大额变动：delta >= 50 才告警 */
    @EventListener(condition = "#event.delta >= 50")
    public void onBigMove(StockMovedEvent event) {
        ALERTS.add(event.getSku() + ":" + event.getDelta());
        System.out.println("[ThresholdAlertListener] condition 命中，大额变动告警: "
                + event.getSku() + " delta=" + event.getDelta());
    }
}
