package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务失效场景四连（外加 rollbackFor 对照）：
 * 1. 自调用：外层方法没有 @Transactional，内部 this.调用带注解的方法——调用不经过代理，注解形同虚设
 * 2. private 方法：CGLIB 无法覆盖 private 方法，反射调用也不经过拦截器链
 * 3. 异常被 catch 吞掉：TransactionInterceptor 看不到异常，按"正常返回"提交
 * 4. 默认回滚规则：只回滚 RuntimeException/Error，受检异常（checked）默认不回滚——rollbackFor 可改
 */
public class FailureScenarioService {

    private final JdbcTemplate jdbcTemplate;

    public FailureScenarioService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 生效对照：经代理调用 + 异常传播 → 回滚 */
    @Transactional
    public void updateThenThrow(String name, int delta) {
        update(name, delta);
        throw new IllegalStateException("异常会传播出去 -> 回滚");
    }

    /** 失效场景1-自调用：outerSelfInvoke 本身没有 @Transactional，this.updateThenThrow 绕过代理 */
    public void selfInvokeThenThrow(String name, int delta) {
        // 此时打印 false：外层没有开事务
        System.out.println("[失效-自调用] 外层事务状态 active=" + TransactionSynchronizationManager.isActualTransactionActive());
        this.updateThenThrow(name, delta);
    }

    /**
     * 失效场景2-private 方法：CGLIB 通过"生成子类覆盖方法"实现代理，private 方法无法被覆盖，
     * 反射调用也不会经过拦截器链 -> 注解彻底失效。
     * （实测补充：package-private/protected 方法在 CGLIB 代理下仍会被拦截且事务生效——
     * 但这是文档不保证的行为，规范用法是 @Transactional 只标 public 方法）
     */
    @Transactional
    private void privateThenThrow(String name, int delta) {
        update(name, delta);
        System.out.println("[失效-private] 方法内事务状态 active=" + TransactionSynchronizationManager.isActualTransactionActive()
                + "（false 说明没有事务，注解没生效）");
        throw new IllegalStateException("没有事务，异常也不会触发回滚");
    }

    /** 失效场景3-异常被吞：TransactionInterceptor 感知不到异常，方法"正常返回"照常提交 */
    @Transactional
    public void updateThenSwallow(String name, int delta) {
        update(name, delta);
        try {
            throw new IllegalStateException("被吞掉的异常");
        } catch (IllegalStateException e) {
            System.out.println("[失效-异常被吞] " + e.getMessage() + " -> 事务照常提交");
        }
    }

    /** 失效场景4-checked 异常：默认回滚规则只认 RuntimeException/Error */
    @Transactional
    public void updateThenThrowChecked(String name, int delta) throws Exception {
        update(name, delta);
        throw new Exception("受检异常默认不回滚");
    }

    /** rollbackFor = Exception.class：受检异常也回滚 */
    @Transactional(rollbackFor = Exception.class)
    public void updateThenThrowCheckedWithRollbackFor(String name, int delta) throws Exception {
        update(name, delta);
        throw new Exception("配置 rollbackFor 后受检异常也回滚");
    }

    private void update(String name, int delta) {
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", delta, name);
    }
}
