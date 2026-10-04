package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 声明式事务第一课：@Transactional 的提交与回滚。
 *
 * 对比三件事：
 * 1. 正常返回 → 切面在方法边界替你 commit；
 * 2. 抛 RuntimeException → 切面捕获后替你 rollback（默认回滚规则只认 RuntimeException/Error）；
 * 3. 受检异常（checked exception）默认【不】回滚——这一坑与 rollbackFor 的补救见 TxFailureService。
 *
 * 从测试直接调用本 Bean 的 public 方法会经过 CGLIB 代理，事务切面才会生效；
 * 这也是后面所有"跨 Bean 调用"演示的标准姿势。
 */
public class TransferService {

    private final JdbcTemplate jdbcTemplate;

    public TransferService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 正常路径：方法返回即提交。打印 active 标志证明代理确实开了事务（而非自动提交裸跑） */
    @Transactional
    public void transferCommit(String from, String to, int amount) {
        System.out.println("[TransferService] 进入事务, active="
                + TransactionSynchronizationManager.isActualTransactionActive());
        debit(from, to, amount);
        System.out.println("[TransferService] 方法正常返回 -> 切面提交: " + from + "->" + to + " " + amount);
    }

    /** 异常路径：RuntimeException 传播出方法边界 -> 切面回滚，两步 update 一起消失 */
    @Transactional
    public void transferRollbackOnRuntime(String from, String to, int amount) {
        debit(from, to, amount);
        throw new IllegalStateException("RuntimeException 传播出边界 -> 默认回滚规则命中, 整个事务回滚");
    }

    /** 一借一贷两步 update 放在同一个事务里——转账的经典原子性场景 */
    private void debit(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
    }
}
