package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 裸 PlatformTransactionManager：比 TransactionTemplate 更底层的编程式事务形态。
 *
 * 手动三步曲：getTransaction(定义) -> 业务 SQL -> commit 或 rollback。
 * TransactionTemplate 的 execute 回调只是把这三步样板代码包了起来。
 *
 * 隐藏知识点：DataSourceTransactionManager.getTransaction() 会把拿到的 Connection
 * 绑定到当前线程，因此随后的 JdbcTemplate 操作自动复用这条绑定连接、加入同一事务——
 * 编程式事务与声明式事务在"连接绑定"这一层是同一套机制。
 */
public class RawTxService {

    private final JdbcTemplate jdbcTemplate;

    private final PlatformTransactionManager transactionManager;

    public RawTxService(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionManager = transactionManager;
    }

    /** 提交路径：getTransaction 开启 -> 两条 update 走绑定连接 -> 手动 commit 落库 */
    public int manuallyCommittedTransfer(String from, String to, int amount) {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            int deducted = jdbcTemplate.update(
                    "update account set balance = balance - ? where name = ?", amount, from);
            int credited = jdbcTemplate.update(
                    "update account set balance = balance + ? where name = ?", amount, to);
            int changed = deducted + credited;
            transactionManager.commit(status);
            System.out.println("[RawTxService] 手动 commit，变更行数=" + changed);
            return changed;
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    /** 回滚路径：扣款 SQL 已执行，但手动 rollback 使其不落库 */
    public int manuallyRolledBackDeduction(String from, int amount) {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        int deducted = jdbcTemplate.update(
                "update account set balance = balance - ? where name = ?", amount, from);
        System.out.println("[RawTxService] 扣款已执行（" + deducted + " 行），接下来手动 rollback");
        transactionManager.rollback(status);
        return deducted;
    }
}
