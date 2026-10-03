package com.alec.InnovateX.spring.transaction;

/** 事务内发布的事件：对比 @EventListener 与 @TransactionalEventListener 的接收时机 */
public class TxCompletedEvent {

    private final String message;

    public TxCompletedEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
