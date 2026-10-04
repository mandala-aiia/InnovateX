package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 监听器排序 + 同步多播异常语义的四个监听器（静态嵌套类集中一文件，由 OrderingEventConfig 逐个 @Bean 注册）：
 *
 * - @Order 值小的先执行：同一事件的多个监听器按 @Order 升序排队（不写则优先级最低）。
 * - 默认的 SimpleApplicationEventMulticaster 没有 executor（同步、在发布线程里逐个调用），
 *   某个监听器抛异常会"中断本事件后续监听器"并把异常抛回 publishEvent 调用方——
 *   这也是 @Async 监听器的核心卖点之一：异步栈里的异常不会传播回发布方。
 */
public class StockEventListeners {

    public static final List<String> EXECUTION = new CopyOnWriteArrayList<>();

    /** @Order(10)：先执行 */
    public static class StockAuditListener {

        @Order(10)
        @EventListener
        public void onStockMoved(StockMovedEvent event) {
            EXECUTION.add("audit:" + event.getSku());
            System.out.println("[StockAuditListener] (order=10) 库存盘点: " + event.getSku()
                    + " delta=" + event.getDelta());
        }
    }

    /** @Order(20)：后执行 */
    public static class StockNotifyListener {

        @Order(20)
        @EventListener
        public void onStockMoved(StockMovedEvent event) {
            EXECUTION.add("notify:" + event.getSku());
            System.out.println("[StockNotifyListener] (order=20) 库存通知: " + event.getSku());
        }
    }

    /** 故意抛异常的监听器（order=1，排在最前）：验证同步多播的异常中断 */
    public static class AlarmThrowingListener {

        @Order(1)
        @EventListener
        public void onAlarm(AuditAlarmEvent event) {
            EXECUTION.add("alarm-throwing");
            System.out.println("[AlarmThrowingListener] (order=1) 抛出异常: " + event.getReason());
            throw new IllegalStateException("监听器处理失败: " + event.getReason());
        }
    }

    /** order=2 排在抛异常者之后：同步多播下永远不该被执行到（异步监听才轮得到它） */
    public static class AlarmBackupListener {

        @Order(2)
        @EventListener
        public void onAlarm(AuditAlarmEvent event) {
            EXECUTION.add("alarm-backup-should-never-run");
            System.out.println("[AlarmBackupListener] (order=2) 不该被执行到这里！");
        }
    }
}
