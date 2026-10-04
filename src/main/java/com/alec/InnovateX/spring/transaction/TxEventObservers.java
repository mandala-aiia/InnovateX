package com.alec.InnovateX.spring.transaction;

import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件监听方：两种监听器在事务场景下的接收时机对比。
 *
 * - @EventListener：publishEvent 调用点同步执行——此时事务尚未提交；
 * - @TransactionalEventListener(AFTER_COMMIT)：登记进事务同步器，提交成功后才回调。
 *
 * 铁证设计：监听器里故意【绕过】DataSourceUtils，用 dataSource.getConnection() 开一条
 * 全新的自动提交连接去读"已提交视图"——普通监听器读到的是转账前的旧余额（事务没提交），
 * AFTER_COMMIT 监听器读到的是新余额（事务已提交）。比只打印标志位更有说服力。
 *
 * 实测细节：AFTER_COMMIT 回调发生在"事务同步上下文清理"之前，
 * 所以回调里 isActualTransactionActive() 仍是 true——属正常现象，不是 bug。
 */
public class TxEventObservers {

    /** 接收记录：按到达顺序追加，测试断言顺序与内容 */
    public static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

    private final DataSource dataSource;

    public TxEventObservers(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener
    public void immediateListener(AccountTouchedEvent event) {
        String visible = committedBalance(event.getTo());
        RECEIVED.add("immediate(提交前, 已提交视图余额=" + visible
                + ", active=" + TransactionSynchronizationManager.isActualTransactionActive() + ")");
        System.out.println("[TxEventObservers] @EventListener 收到 " + event.describe() + " -> " + RECEIVED.get(RECEIVED.size() - 1));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommitListener(AccountTouchedEvent event) {
        String visible = committedBalance(event.getTo());
        RECEIVED.add("afterCommit(提交后, 已提交视图余额=" + visible
                + ", active=" + TransactionSynchronizationManager.isActualTransactionActive() + ")");
        System.out.println("[TxEventObservers] @TransactionalEventListener 收到 " + event.describe() + " -> " + RECEIVED.get(RECEIVED.size() - 1));
    }

    /**
     * BEFORE_COMMIT：提交前的最后一个同步点——做最终校验/兜底的时机，
     * 此刻抛异常可以直接否决整个提交（比 AFTER_COMMIT 早，但事务内修改已全部执行完）
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommitListener(AccountTouchedEvent event) {
        String visible = committedBalance(event.getTo());
        RECEIVED.add("beforeCommit(提交前最后关口, 已提交视图余额=" + visible + ")");
        System.out.println("[TxEventObservers] BEFORE_COMMIT 收到 " + event.describe()
                + " -> " + RECEIVED.get(RECEIVED.size() - 1));
    }

    /** AFTER_ROLLBACK：只有回滚路径才收到（提交路径与无事务都不投递）——"善后/告警"的挂钩点 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollbackListener(AccountTouchedEvent event) {
        String visible = committedBalance(event.getTo());
        RECEIVED.add("afterRollback(回滚后, 已提交视图余额=" + visible + ")");
        System.out.println("[TxEventObservers] AFTER_ROLLBACK 收到 " + event.describe()
                + " -> " + RECEIVED.get(RECEIVED.size() - 1));
    }

    /** 新开一条自动提交连接读"已提交视图"——看不到别的事务未提交的修改 */
    private String committedBalance(String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "select balance from account where name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("读取已提交余额失败", e);
        }
    }
}
