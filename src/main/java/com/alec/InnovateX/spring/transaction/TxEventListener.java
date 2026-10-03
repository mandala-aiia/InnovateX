package com.alec.InnovateX.spring.transaction;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 两种事件监听器在事务场景下的接收时机对比：
 * - @EventListener：publishEvent 的调用点同步执行——此时事务还没提交
 * - @TransactionalEventListener(AFTER_COMMIT)：等事务成功提交后才回调。
 *   注意实测细节：AFTER_COMMIT 回调发生在"事务同步上下文清理"之前，
 *   所以此时 isActualTransactionActive() 仍是 true（提交后的清理阶段才置 false）
 */
@Component
public class TxEventListener {

    public static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

    @EventListener
    public void plainListener(TxCompletedEvent event) {
        RECEIVED.add("plain(事务未提交, active=" + TransactionSynchronizationManager.isActualTransactionActive() + "): " + event.getMessage());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommitListener(TxCompletedEvent event) {
        RECEIVED.add("AFTER_COMMIT(提交后回调, active=" + TransactionSynchronizationManager.isActualTransactionActive() + "): " + event.getMessage());
    }
}
