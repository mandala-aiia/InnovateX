package com.alec.InnovateX.spring.transaction;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务外层服务：@Transactional 基本回滚 + 三大传播行为对比 + 事务绑定事件 + 隔离级别查看。
 * 嵌套调用必须跨 Bean（注入 PropagationInnerService），否则自调用绕过代理、事务失效
 */
public class TransferService {

    private final JdbcTemplate jdbcTemplate;

    private final PropagationInnerService inner;

    private final ApplicationEventPublisher eventPublisher;

    public TransferService(JdbcTemplate jdbcTemplate, PropagationInnerService inner, ApplicationEventPublisher eventPublisher) {
        this.jdbcTemplate = jdbcTemplate;
        this.inner = inner;
        this.eventPublisher = eventPublisher;
    }

    /** 非事务读余额（演示断言用） */
    public int balance(String name) {
        return jdbcTemplate.queryForObject("select balance from account where name = ?", Integer.class, name);
    }

    @Transactional
    public void transferSuccess(String from, String to, int amount) {
        transfer(from, to, amount);
    }

    /** 基本回滚：异常传播出 @Transactional 方法，整个事务回滚 */
    @Transactional
    public void transferThenRollback(String from, String to, int amount) {
        transfer(from, to, amount);
        throw new IllegalStateException("外层主动抛异常，整个事务回滚");
    }

    /**
     * REQUIRED 内层失败：内层异常把共享事务标记为 rollback-only，
     * 即使外层 catch 吞掉异常、方法正常返回，提交时也会抛 UnexpectedRollbackException（同生共死）
     */
    @Transactional
    public void outerRequiredInnerFails(String from, String to, int amount) {
        transfer(from, to, amount);
        try {
            inner.innerRequiredThenThrow("carol", "dave", amount);
        } catch (IllegalStateException e) {
            System.out.println("[TransferService] 内层异常被外层吞掉: " + e.getMessage());
        }
    }

    /**
     * REQUIRES_NEW 内层失败：内层是独立事务自己回滚，外层事务完全不受影响照常提交
     * 预期：alice->bob 生效，carol->dave 回滚
     */
    @Transactional
    public void outerRequiresNewInnerFails(String from, String to, int amount) {
        transfer(from, to, amount);
        try {
            inner.innerRequiresNewThenThrow("carol", "dave", amount);
        } catch (IllegalStateException e) {
            System.out.println("[TransferService] 内层 REQUIRES_NEW 自己回滚: " + e.getMessage());
        }
    }

    /**
     * NESTED 内层失败：回滚只到 savepoint，外层捕获后继续提交自己的修改
     * 预期：alice->bob 生效，carol->dave 回滚（现象与 REQUIRES_NEW 相同，但机制是同一事务内的保存点）
     */
    @Transactional
    public void outerNestedInnerFails(String from, String to, int amount) {
        transfer(from, to, amount);
        try {
            inner.innerNestedThenThrow("carol", "dave", amount);
        } catch (IllegalStateException e) {
            System.out.println("[TransferService] 内层 NESTED 回滚到 savepoint: " + e.getMessage());
        }
    }

    /** MANDATORY：没有外层事务时直接拒绝 */
    public void callMandatoryWithoutTx() {
        inner.mandatory();
    }

    /** NEVER：存在外层事务时直接拒绝 */
    @Transactional
    public void callNeverWithTx() {
        inner.never();
    }

    /** NOT_SUPPORTED：方法执行时外层事务被挂起 */
    @Transactional
    public String callNotSupportedWithTx() {
        return inner.notSupported();
    }

    /** SUPPORTS：没有事务时也照常（非事务）执行 */
    public String callSupportsWithoutTx() {
        return inner.supports();
    }

    /** 事务内发布事件：@EventListener 立刻收到，@TransactionalEventListener 等提交后收到 */
    @Transactional
    public void transferWithEvent(String from, String to, int amount) {
        transfer(from, to, amount);
        eventPublisher.publishEvent(new TxCompletedEvent(from + "->" + to + ":" + amount));
        System.out.println("[TransferService] 事件已发布（此刻事务尚未提交）");
    }

    /** 查看当前事务隔离级别：DataSourceUtils 拿到的就是事务绑定的那个 Connection */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String currentIsolation() throws SQLException {
        Connection connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        return switch (connection.getTransactionIsolation()) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN";
        };
    }

    private void transfer(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
    }
}
