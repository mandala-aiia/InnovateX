package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 裸 PlatformTransactionManager 编程式事务 + NamedParameterJdbcTemplate：
 * 这是比 TransactionTemplate 更底层的形态——手动 getTransaction/commit/rollback，
 * TransactionTemplate 只是把这套样板代码收进 execute 回调。
 * NamedParameterJdbcTemplate 用 :name 占位符替代 ?，参数再多也与顺序无关
 */
public class ProgrammaticTxService {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final PlatformTransactionManager transactionManager;

    public ProgrammaticTxService(JdbcTemplate jdbcTemplate,
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.transactionManager = transactionManager;
    }

    /** 提交路径：getTransaction 开启事务 -> 业务 SQL -> 手动 commit */
    public int committedTransfer(String from, String to, int amount) {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            int changed = jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from)
                    + jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
            transactionManager.commit(status);
            System.out.println("[ProgrammaticTxService] 手动 commit，变更行数=" + changed);
            return changed;
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    /** 回滚路径：扣款 SQL 已执行，但手动 rollback 使其不落库 */
    public int rolledBackTransfer(String from, String to, int amount) {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        int deducted = jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        System.out.println("[ProgrammaticTxService] 已扣款 " + deducted + " 行，手动 rollback");
        transactionManager.rollback(status);
        return deducted;
    }

    /** NamedParameterJdbcTemplate：:name 命名占位符，参数按名绑定 */
    public int namedParameterInsert(String orderNo, int amount) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderNo", orderNo)
                .addValue("amount", amount);
        int rows = namedParameterJdbcTemplate.update(
                "insert into order_log (order_no, amount) values (:orderNo, :amount)", params);
        System.out.println("[ProgrammaticTxService] 命名参数插入 order_log，行数=" + rows);
        return rows;
    }
}
