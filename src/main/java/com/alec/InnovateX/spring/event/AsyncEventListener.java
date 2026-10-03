package com.alec.InnovateX.spring.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 异步事件监听器：@Async + @EventListener 组合，事件处理丢给线程池执行，
 * publishEvent 不再被监听器的耗时逻辑阻塞，监听器异常也不会传播给发布方
 */
@Component
public class AsyncEventListener {

    public static volatile String threadName;

    public static final CountDownLatch LATCH = new CountDownLatch(1);

    @Async
    @EventListener
    public void onOrderCreatedAsync(OrderCreatedEvent event) throws InterruptedException {
        threadName = Thread.currentThread().getName();
        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println("[AsyncEventListener] 异步处理订单事件: " + event.getOrderNo()
                + "，线程=" + threadName);
        LATCH.countDown();
    }
}
