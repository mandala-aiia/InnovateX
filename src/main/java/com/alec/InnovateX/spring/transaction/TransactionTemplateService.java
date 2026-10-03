package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionTemplate：编程式事务的现代封装。
 * 对比同包已有的两种形态——手动 PlatformTransactionManager（最底层）与 @Transactional（声明式），
 * 模板把 begin/commit/rollback 的样板代码收进 execute 回调，粒度可以精确到代码块。
 * status.setRollbackOnly()：不抛异常也能标记回滚（等效"吞掉异常但必须回滚"的场景）
 */
public class TransactionTemplateService {

    private final JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    public TransactionTemplateService(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    /** @param rollbackOnly true 时用 setRollbackOnly 标记回滚（异常不向外传播） */
    public String transfer(String from, String to, int amount, boolean rollbackOnly) {
        return transactionTemplate.execute(status -> {
            jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
            jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
            if (rollbackOnly) {
                status.setRollbackOnly();
            }
            System.out.println("[TransactionTemplateService] execute 内 rollbackOnly=" + rollbackOnly
                    + "，isNewTransaction=" + status.isNewTransaction());
            return rollbackOnly ? "已标记回滚（setRollbackOnly）" : "已提交";
        });
    }

    /** 传播行为、隔离级别、超时等都可以直接在模板上编程式配置 */
    public void configureReadOnly() {
        transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setReadOnly(true);
        System.out.println("[TransactionTemplateService] 模板已改为 REQUIRES_NEW + readOnly");
    }
}
