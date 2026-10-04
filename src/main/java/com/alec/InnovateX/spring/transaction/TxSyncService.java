package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事务同步器与"事务-连接绑定"原理：
 *
 * 1) TransactionSynchronizationManager 本质是一组 ThreadLocal，是当前事务的"随身物品架"：
 *    - ConnectionHolder：线程绑定的数据库连接。JdbcTemplate/DataSourceUtils 都从这里取连接，
 *      这就是"同一事务内所有操作共用一条 Connection 提交/回滚"的底层机制；
 *    - synchronizations：事务同步回调列表——@TransactionalEventListener 的底层就是它注册的钩子。
 * 2) 手动 registerSynchronization 可以挂进事务提交/回滚的各个阶段：
 *    beforeCommit -> beforeCompletion -> (commit/rollback) -> afterCommit -> afterCompletion；
 *    suspend/resume 则只在嵌套事务挂起/恢复当前事务时触发（这里借 REQUIRES_NEW 内层触发它们）。
 * 3) DataSourceUtils.releaseConnection 对"绑定中的连接"只是归还引用计数，不物理关闭——
 *    连接的生命周期归事务管理器管，提交/回滚后才真正释放。
 */
public class TxSyncService {

    /** 观察记录：方法内检查项 + 各阶段回调按发生顺序追加，测试对整个序列做精确断言 */
    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    private final PropagationInnerService inner;

    public TxSyncService(JdbcTemplate jdbcTemplate, DataSource dataSource, PropagationInnerService inner) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.inner = inner;
    }

    /**
     * 提交路径的完整生命周期：
     * 事务内先验证连接绑定（两次 getConnection 是同一个对象），
     * 再调用一个 REQUIRES_NEW 内层触发 suspend/resume，
     * 方法返回后外层提交，依次走 beforeCommit/beforeCompletion/afterCommit/afterCompletion。
     */
    @Transactional
    public void commitPathFullLifecycle() {
        // 事务已开启：连接应通过 ConnectionHolder 绑定在当前线程
        EVENTS.add("bound=" + TransactionSynchronizationManager.hasResource(dataSource));

        // 两次 DataSourceUtils.getConnection 取的都是"线程绑定的那一条"——不是新开连接
        Connection c1 = DataSourceUtils.getConnection(dataSource);
        Connection c2 = DataSourceUtils.getConnection(dataSource);
        EVENTS.add("sameConnection=" + (c1 == c2));

        // 注册全阶段回调
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
                EVENTS.add("afterCompletion:" + statusText(status));
            }
        });

        // 嵌套独立事务：挂起外层(suspend) -> 内层提交 -> 恢复外层(resume)
        inner.requiresNewCredit("carol", "dave", 50);

        // 外层自己的修改（JdbcTemplate 内部同样走绑定连接）
        jdbcTemplate.update("update account set balance = balance - 100 where name = 'alice'");
        jdbcTemplate.update("update account set balance = balance + 100 where name = 'bob'");

        // 归还绑定而非物理关闭——真正的释放发生在事务完成之后
        DataSourceUtils.releaseConnection(c2, dataSource);
        DataSourceUtils.releaseConnection(c1, dataSource);
    }

    /** 回滚路径：beforeCompletion 与 afterCompletion(ROLLED_BACK) 会触发，afterCommit 不会 */
    @Transactional
    public void rollbackPathLifecycle() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCompletion() {
                EVENTS.add("beforeCompletion");
            }

            @Override
            public void afterCompletion(int status) {
                EVENTS.add("afterCompletion:" + statusText(status));
            }
        });
        jdbcTemplate.update("update account set balance = balance + 999 where name = 'alice'");
        throw new IllegalStateException("回滚路径：这行 +999 不会落库");
    }

    private static String statusText(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            return "COMMITTED";
        }
        if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
            return "ROLLED_BACK";
        }
        return "UNKNOWN";
    }
}
