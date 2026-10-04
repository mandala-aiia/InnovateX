package com.alec.InnovateX.spring.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监听器排序与异常传播演示装配（独立配置，与 CoreEventConfig 的事件类型互不干扰）。
 */
@Configuration
public class OrderingEventConfig {

    @Bean
    public StockEventListeners.StockAuditListener stockAuditListener() {
        return new StockEventListeners.StockAuditListener();
    }

    @Bean
    public StockEventListeners.StockNotifyListener stockNotifyListener() {
        return new StockEventListeners.StockNotifyListener();
    }

    @Bean
    public StockEventListeners.AlarmThrowingListener alarmThrowingListener() {
        return new StockEventListeners.AlarmThrowingListener();
    }

    @Bean
    public StockEventListeners.AlarmBackupListener alarmBackupListener() {
        return new StockEventListeners.AlarmBackupListener();
    }

    @Bean
    public ThresholdAlertListener thresholdAlertListener() {
        return new ThresholdAlertListener();
    }
}
