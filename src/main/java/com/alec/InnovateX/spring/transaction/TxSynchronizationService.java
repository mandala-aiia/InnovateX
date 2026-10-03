package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事务同步器与"事务-连接绑定"原理：
 * 1) TransactionSynchronizationManager 是事务的"随身物品架"（一堆 ThreadLocal）：
 *    - ConnectionHolder：当前线程绑定的数据库连接（DataSourceUtils/JdbcTemplate 都从这里取）
 *    - synchronizations：事务同步回调列表（@TransactionalEventListener 的底层就是它注册的回调）
 * 2) 同一事务内 DataSourceUtils.getConnection 拿到的是同一个连接——
 *    这就是"整个事务共用一个 Connection 提交/回滚"的底层机制
 * 3) 手动注册 TransactionSynchronization 可在提交/回滚的各阶段挂钩子
 */
public class TxSynchronizationService {

    /** 记录事件顺序：方法内观察项 + 各阶段回调 */
    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public TxSynchronizationService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Transactional
    public void workWithSynchronization() {
        // 事务内：连接已通过 ConnectionHolder 绑定到当前线程
        EVENTS.add("bound=" + TransactionSynchronizationManager.hasResource(dataSource));

        // 两次 DataSourceUtils.getConnection 拿到的是同一个绑定连接（不新开连接）
        java.sql.Connection c1 = DataSourceUtils.getConnection(dataSource);
        java.sql.Connection c2 = DataSourceUtils.getConnection(dataSource);
        EVENTS.add("sameConnection=" + (c1 == c2));

        // 手动注册事务同步回调：提交/回滚各阶段被钩住
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void suspend() {
                EVENTS.add("suspend");
            }

            @Override
            public void resume() {
                EVENTS.add("resume");
            }

            @Override
            public void beforeCommit(boolean readOnly) {
                EVENTS.add("beforeCommit(readOnly=" + readOnly + ")");
            }

            @Override
            public void beforeCompletion() {
                EVENTS.add("beforeCompletion");
            }

            @Override
            public void afterCommit() {
                EVENTS.add("afterCommit");
            }

            @Override
            public void afterCompletion(int status) {
                String statusText = status == TransactionSynchronization.STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK";
                EVENTS.add("afterCompletion:" + statusText);
            }
        });

        // 正常执行一条 update（JdbcTemplate 内部也用 DataSourceUtils 拿同一个绑定连接）
        jdbcTemplate.update("update account set balance = balance where id = 1");

        // 绑定连接上的 releaseConnection 是"归还绑定"而非物理关闭
        DataSourceUtils.releaseConnection(c1, dataSource);
        DataSourceUtils.releaseConnection(c2, dataSource);
    }

    /** 异常路径：afterCompletion 收到 ROLLED_BACK 状态 */
    @Transactional
    public void workWithSynchronizationAndRollback() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                String statusText = status == TransactionSynchronization.STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK";
                EVENTS.add("rollback-afterCompletion:" + statusText);
            }
        });
        throw new IllegalStateException("触发回滚");
    }
}
