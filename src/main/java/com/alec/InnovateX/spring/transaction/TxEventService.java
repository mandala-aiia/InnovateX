package com.alec.InnovateX.spring.transaction;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事件发布方：在 @Transactional 事务【内部】发布事件。
 *
 * publishEvent 只是"登记"，两个监听器的接收时机截然不同：
 * - @EventListener：publish 调用点同步执行——此刻事务还没提交；
 * - @TransactionalEventListener(AFTER_COMMIT)：登记到事务同步器，提交成功后才回调。
 * 这就是"发消息通知外部系统"必须用 AFTER_COMMIT 的原因——否则事务万一回滚，
 * 外部系统已经收到了一条与数据库状态不一致的通知。
 */
public class TxEventService {

    private final JdbcTemplate jdbcTemplate;

    private final ApplicationEventPublisher publisher;

    public TxEventService(JdbcTemplate jdbcTemplate, ApplicationEventPublisher publisher) {
        this.jdbcTemplate = jdbcTemplate;
        this.publisher = publisher;
    }

    /** 事务内转账并发布事件：普通监听器立刻收到，事务监听器要等提交后 */
    @Transactional
    public void transferAndPublish(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
        publisher.publishEvent(new AccountTouchedEvent(from, to, amount));
        System.out.println("[TxEventService] 事件已发布(" + from + "->" + to + ":" + amount
                + ")，此刻事务尚未提交");
    }

    /**
     * 无事务发布：@TransactionalEventListener 默认 fallbackExecution=false，
     * 没有事务就不投递——这条事件只有普通监听器能收到。
     */
    public void publishOutsideTransaction() {
        publisher.publishEvent(new AccountTouchedEvent("alice", "alice", 0));
        System.out.println("[TxEventService] 无事务发布事件：AFTER_COMMIT 监听器将被静默跳过");
    }

    /**
     * 回滚路径：事务内发布事件后抛异常——普通监听器照常收到（发布即达），
     * 事务监听器里只有 AFTER_ROLLBACK 收到，AFTER_COMMIT/BEFORE_COMMIT 全部静默跳过
     */
    @Transactional
    public void transferAndPublishThenRollback(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
        publisher.publishEvent(new AccountTouchedEvent(from, to, amount));
        throw new IllegalStateException("故意回滚: " + from + "->" + to + ":" + amount);
    }
}
