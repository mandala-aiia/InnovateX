package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

/**
 * 七种传播行为的"内层 Bean"。
 *
 * 关键前提：传播行为只在"经过代理的跨 Bean 调用"上才有意义。
 * 如果在同一个类里 this.xxx() 调用（自调用），调用不经过代理拦截器链，
 * @Transactional 与一切传播设置都形同虚设——所以本类只做"被调用的内层"，
 * 由 PropagationOuterService 注入并调用。
 *
 * 七种行为一句话对照：
 * - REQUIRED（默认）：有事务就加入，没有就新建——与外层"同生共死"；
 * - REQUIRES_NEW：挂起外层，新开完全独立的物理事务（第二条连接），两边互不影响；
 * - NESTED：在外层事务【内部】打 savepoint，内层失败只回滚到保存点，外层可继续提交；
 * - SUPPORTS：随遇而安——有事务就加入，没有就以非事务（自动提交）方式执行；
 * - NOT_SUPPORTED：挂起外层事务，方法体一定以非事务方式执行；
 * - MANDATORY：要求调用方必须有事务，否则抛 IllegalTransactionStateException；
 * - NEVER：要求调用方必须没有事务，否则抛 IllegalTransactionStateException。
 */
public class PropagationInnerService {

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public PropagationInnerService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /** REQUIRED 正向：加入外层事务，与外层一起提交 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredJoinCredit(String from, String to, int amount) {
        credit(from, to, amount);
        System.out.println("[PropagationInnerService] REQUIRED 加入外层事务, active="
                + TransactionSynchronizationManager.isActualTransactionActive());
    }

    /** REQUIRED 反向：抛异常把【共享事务】标记为 rollback-only——外层即使吞掉异常也逃不掉 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredCreditThenThrow(String from, String to, int amount) {
        credit(from, to, amount);
        throw new IllegalStateException("内层 REQUIRED 异常: 共享事务已被标记 rollback-only");
    }

    /** REQUIRES_NEW 正向：独立新事务正常提交——之后即使外层回滚，这笔修改也存活 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewCredit(String from, String to, int amount) {
        credit(from, to, amount);
        System.out.println("[PropagationInnerService] REQUIRES_NEW 独立事务已提交: " + from + "->" + to + " " + amount);
    }

    /** REQUIRES_NEW 反向：独立事务自己抛异常回滚，被挂起的外层不受任何影响 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewCreditThenThrow(String from, String to, int amount) {
        credit(from, to, amount);
        throw new IllegalStateException("内层 REQUIRES_NEW 异常: 只有这个独立事务回滚");
    }

    /** NESTED：外层事务内打 savepoint；异常时只回滚到保存点，外层可捕获后继续提交自己的修改 */
    @Transactional(propagation = Propagation.NESTED)
    public void nestedCreditThenThrow(String from, String to, int amount) {
        credit(from, to, amount);
        throw new IllegalStateException("内层 NESTED 异常: 只回滚到 savepoint, 不拖累外层");
    }

    /**
     * SUPPORTS：有事务就加入（active=true），没有就以非事务方式执行（active=false）。
     * 返回执行时的事务状态，两种调用场景可以对照出完全不同的结果。
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public String supportsCredit(String name, int delta) {
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", delta, name);
        return "supports: active=" + TransactionSynchronizationManager.isActualTransactionActive();
    }

    /**
     * NOT_SUPPORTED：外层事务被挂起后本方法以非事务方式执行，update 走自动提交的新连接立即落库。
     * 实测的坑：挂起时 Spring 只解绑 ConnectionHolder、清空同步器列表，但【不会重置】
     * actualTransactionActive 标志——所以该标志可能是残留的 true；
     * 判断"是否真的在事务里"要看连接是否仍绑定在线程上（hasResource）。
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String notSupportedCredit(String name, int delta) {
        boolean activeFlag = TransactionSynchronizationManager.isActualTransactionActive();
        boolean connectionBound = TransactionSynchronizationManager.hasResource(dataSource);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", delta, name);
        return "notSupported: activeFlag=" + activeFlag + "(可能是残留标志), connectionBound=" + connectionBound
                + "(连接已解绑=真的没有事务, 本条 update 自动提交)";
    }

    /** MANDATORY：必须有外层事务才允许执行 */
    @Transactional(propagation = Propagation.MANDATORY)
    public String mandatoryPing() {
        return "MANDATORY 正常执行, active=" + TransactionSynchronizationManager.isActualTransactionActive();
    }

    /** NEVER：必须没有外层事务才允许执行 */
    @Transactional(propagation = Propagation.NEVER)
    public String neverPing() {
        return "NEVER 正常执行, active=" + TransactionSynchronizationManager.isActualTransactionActive();
    }

    private void credit(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
    }
}
