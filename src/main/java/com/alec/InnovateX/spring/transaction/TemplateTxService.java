package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionTemplate：编程式事务的现代封装。
 *
 * 与同包另外两种形态的对照：
 * - @Transactional（TransferService）：声明式，事务边界=方法边界，最省事但粒度固定；
 * - 裸 PlatformTransactionManager（RawTxService）：最底层，begin/commit/rollback 全手写；
 * - TransactionTemplate（本类）：把样板代码收进 execute 回调，事务边界精确到任意代码块，
 *   且传播行为/隔离级别等可以【运行期编程配置】——这是注解方式做不到的动态性。
 *
 * status.setRollbackOnly()：不抛异常也能标记回滚，execute 正常返回——
 * 适合"失败不算错误、但不能提交"的业务语义。
 */
public class TemplateTxService {

    private final JdbcTemplate jdbcTemplate;

    /** 注入的共享模板：保持默认 REQUIRED 传播 */
    private final TransactionTemplate sharedTemplate;

    private final PlatformTransactionManager transactionManager;

    public TemplateTxService(JdbcTemplate jdbcTemplate,
                             TransactionTemplate sharedTemplate,
                             PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.sharedTemplate = sharedTemplate;
        this.transactionManager = transactionManager;
    }

    /** 基础1：execute 回调正常返回 -> 提交 */
    public String transferViaExecute(String from, String to, int amount) {
        return sharedTemplate.execute(status -> {
            credit(from, to, amount);
            System.out.println("[TemplateTxService] execute 内 isNewTransaction=" + status.isNewTransaction()
                    + "，正常返回 -> 提交");
            return "committed";
        });
    }

    /** 基础2：内部 catch 掉异常 + setRollbackOnly -> 静默回滚，execute 照常返回值、不外抛 */
    public String transferThenSilentRollback(String from, String to, int amount) {
        return sharedTemplate.execute(status -> {
            try {
                credit(from, to, amount);
                throw new IllegalStateException("模拟一次失败");
            } catch (IllegalStateException e) {
                status.setRollbackOnly();
                System.out.println("[TemplateTxService] 异常被 catch + setRollbackOnly -> 回滚但方法正常返回");
                return "rollback-only";
            }
        });
    }

    /**
     * 编程式配置传播与隔离：模板本身就是一个"可复制的配置对象"——
     * 现场 new 一个 REQUIRES_NEW + REPEATABLE_READ 的独立模板，套在会失败的外层模板里：
     * 内层独立事务提交后，外层故意抛异常回滚，内层修改依然存活；
     * 同时在回调内读 TransactionSynchronizationManager.getCurrentTransactionIsolationLevel()，
     * 证明 setIsolationLevel 确实作用到了当前事务。
     */
    public String requiresNewWithCustomIsolationInsideFailingOuter(String innerFrom, String innerTo, int amount) {
        TransactionTemplate independent = new TransactionTemplate(transactionManager);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        independent.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        int[] isolationHolder = new int[1];
        try {
            sharedTemplate.execute(outerStatus -> {
                credit("alice", "bob", 100);   // 外层自己的修改，稍后随外层回滚
                isolationHolder[0] = independent.execute(innerStatus -> {
                    credit(innerFrom, innerTo, amount);
                    Integer level = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
                    System.out.println("[TemplateTxService] 独立模板内隔离级别=" + level
                            + "（ISOLATION_REPEATABLE_READ=4）");
                    return level;
                });
                throw new IllegalStateException("外层故意失败——REQUIRES_NEW 内层已提交，不受牵连");
            });
        } catch (IllegalStateException e) {
            System.out.println("[TemplateTxService] 外层模板已回滚: " + e.getMessage());
        }
        return "innerIsolation=" + isolationHolder[0];
    }

    private void credit(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
    }
}
