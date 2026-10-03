package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 传播行为的"内层"：必须由另一个 Bean 调用才能经过代理（自调用事务失效）。
 * 7 种传播行为的关键差异：
 * - REQUIRED（默认）：有事务就加入，没有就新建
 * - REQUIRES_NEW：总是挂起当前事务、新开一个完全独立的事务（两个物理连接）
 * - NESTED：在当前事务内创建 savepoint，内层回滚只回到 savepoint，外层可选择继续提交
 * - SUPPORTS：有事务就加入，没有就以非事务方式执行
 * - NOT_SUPPORTED：总是挂起事务，以非事务方式执行
 * - MANDATORY：必须有已存在的事务，否则抛 IllegalTransactionStateException
 * - NEVER：必须没有事务，有则抛 IllegalTransactionStateException
 */
public class PropagationInnerService {

    private final JdbcTemplate jdbcTemplate;

    private final javax.sql.DataSource dataSource;

    public PropagationInnerService(JdbcTemplate jdbcTemplate, javax.sql.DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /** REQUIRED：加入外层事务，抛异常把共享事务标记为 rollback-only */
    @Transactional(propagation = Propagation.REQUIRED)
    public void innerRequiredThenThrow(String from, String to, int amount) {
        transfer(from, to, amount);
        throw new IllegalStateException("内层 REQUIRED 异常");
    }

    /** REQUIRES_NEW：独立新事务，回滚不影响被挂起的外层事务 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerRequiresNewThenThrow(String from, String to, int amount) {
        transfer(from, to, amount);
        throw new IllegalStateException("内层 REQUIRES_NEW 异常");
    }

    /** NESTED：savepoint 内回滚，外层捕获异常后可以继续提交自己的修改 */
    @Transactional(propagation = Propagation.NESTED)
    public void innerNestedThenThrow(String from, String to, int amount) {
        transfer(from, to, amount);
        throw new IllegalStateException("内层 NESTED 异常");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String mandatory() {
        return "MANDATORY：有事务，正常执行";
    }

    @Transactional(propagation = Propagation.NEVER)
    public String never() {
        return "NEVER：没有事务，正常执行";
    }

    /**
     * NOT_SUPPORTED：挂起外层事务，方法以非事务方式执行。
     * 注意实测的坑：挂起时 Spring 只解绑 Connection、清除同步列表，并不重置 actualTransactionActive 标志，
     * 所以方法内 isActualTransactionActive() 可能仍是 true——判断"是否真的在事务里"要看连接是否绑定（hasResource）
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String notSupported() {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        boolean connectionBound = TransactionSynchronizationManager.hasResource(dataSource);
        return "notSupported 内 active=" + active + "（残留标志）， connectionBound=" + connectionBound + "（连接已解绑=真的没事务）";
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public String supports() {
        return "supports 内 isActualTransactionActive=" + TransactionSynchronizationManager.isActualTransactionActive();
    }

    private void transfer(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
    }
}
