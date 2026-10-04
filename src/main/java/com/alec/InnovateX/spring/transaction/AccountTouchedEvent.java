package com.alec.InnovateX.spring.transaction;

/**
 * 事务内发布的事件载荷：对比 @EventListener 与 @TransactionalEventListener 的接收时机。
 * 普通类 + 显式 getter，不用 lombok。
 */
public class AccountTouchedEvent {

    private final String from;

    private final String to;

    private final int amount;

    public AccountTouchedEvent(String from, String to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getAmount() {
        return amount;
    }

    public String describe() {
        return from + "->" + to + ":" + amount;
    }
}
