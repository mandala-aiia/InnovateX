package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CountDownLatch;

/**
 * @Async 异步监听器：@Async + @EventListener 组合（两个注解都在方法上，顺序无关）。
 *
 * 效果：publishEvent 立即返回，事件处理被提交给自定义线程池——
 * 1) 发布方不被慢监听器阻塞；2) 监听器的异常在异步执行栈里，不会传播回发布方。
 *
 * 同步等待用 CountDownLatch（测试绝不裸 sleep）：监听器完成即 countDown。
 * workerThreadName 记录实际执行线程名，测试断言它来自自定义 executor 的线程名前缀。
 */
public class ShipmentAsyncListener {

    public static final CountDownLatch DONE = new CountDownLatch(1);

    public static volatile String workerThreadName;

    @Async
    @EventListener
    public void onShipmentDispatched(ShipmentDispatchedEvent event) {
        workerThreadName = Thread.currentThread().getName();
        System.out.println("[ShipmentAsyncListener] 异步处理发货事件: " + event.getTrackingNo()
                + "，工作线程=" + workerThreadName);
        DONE.countDown();
    }
}
