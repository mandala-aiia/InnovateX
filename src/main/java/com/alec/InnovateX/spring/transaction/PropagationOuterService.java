package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * 七种传播行为的"外层 Bean"：注入 PropagationInnerService 做跨 Bean 调用。
 *
 * 为什么要跨 Bean：@Transactional 的实现是"代理拦截方法调用"，
 * 同类里的 this.inner() 调用不走代理，传播行为全部失效；注入另一个 Bean 再调用，
 * 才能保证请求经过代理拦截器链，传播行为真正参与裁决。
 *
 * 另外演示一个冷门但好用的 API：TransactionAspectSupport.currentTransactionStatus()
 * ——在 @Transactional 方法体内拿到当前事务状态并 setRollbackOnly()，
 * 实现"不抛异常也能让外层事务回滚"。
 */
public class PropagationOuterService {

    private final JdbcTemplate jdbcTemplate;

    private final PropagationInnerService inner;

    public PropagationOuterService(JdbcTemplate jdbcTemplate, PropagationInnerService inner) {
        this.jdbcTemplate = jdbcTemplate;
        this.inner = inner;
    }

    /** REQUIRED 加入：外层先转账，内层 REQUIRED 加入同一事务，两边一起提交 */
    @Transactional
    public void requiredJoinAndCommitTogether() {
        debit("alice", "bob", 60);
        inner.requiredJoinCredit("carol", "dave", 40);
        System.out.println("[PropagationOuterService] REQUIRED 加入: 外层 alice->bob60 与内层 carol->dave40 同事务提交");
    }

    /**
     * REQUIRED 同生共死：内层 REQUIRED 抛异常把共享事务标记 rollback-only，
     * 外层虽然 catch 吞掉了异常、方法"正常返回"，但提交时仍是回滚并抛 UnexpectedRollbackException——
     * 加入即绑定命运，吞异常救不了。
     */
    @Transactional
    public void requiredInnerFailsOuterSwallows() {
        debit("alice", "bob", 100);
        try {
            inner.requiredCreditThenThrow("carol", "dave", 50);
        } catch (IllegalStateException e) {
            System.out.println("[PropagationOuterService] 内层异常被外层吞掉: " + e.getMessage());
        }
    }

    /** REQUIRES_NEW 方向一：内层独立事务自己回滚，外层不受影响照常提交 */
    @Transactional
    public void requiresNewInnerFailsOuterCommits() {
        debit("alice", "bob", 100);
        try {
            inner.requiresNewCreditThenThrow("carol", "dave", 50);
        } catch (IllegalStateException e) {
            System.out.println("[PropagationOuterService] 内层 REQUIRES_NEW 自己回滚, 外层照常: " + e.getMessage());
        }
    }

    /** REQUIRES_NEW 方向二：内层独立事务先提交，随后外层失败回滚——内层修改存活 */
    @Transactional
    public void requiresNewInnerCommitsOuterFails() {
        inner.requiresNewCredit("carol", "dave", 50);
        debit("alice", "bob", 100);
        throw new IllegalStateException("外层失败: REQUIRES_NEW 内层已独立提交, 不受牵连");
    }

    /** NESTED：内层回滚只到 savepoint，外层捕获后继续提交自己的修改（与 REQUIRES_NEW 现象同、机制异） */
    @Transactional
    public void nestedInnerFailsOuterCommits() {
        debit("alice", "bob", 100);
        try {
            inner.nestedCreditThenThrow("carol", "dave", 50);
        } catch (IllegalStateException e) {
            System.out.println("[PropagationOuterService] 内层 NESTED 只回滚到保存点, 外层继续提交: " + e.getMessage());
        }
    }

    /** SUPPORTS 场景一：有外层事务时 SUPPORTS 加入，随后外层 setRollbackOnly 回滚——加入的修改一起消失 */
    @Transactional
    public String supportsJoinThenOuterRollsBack() {
        String report = inner.supportsCredit("alice", 100);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        System.out.println("[PropagationOuterService] SUPPORTS 已加入外层, 外层标记回滚 -> 内层修改一并回滚");
        return report;
    }

    /** SUPPORTS 场景二：没有外层事务时 SUPPORTS 以非事务方式执行，update 自动提交立即落库 */
    public String supportsWithoutOuterTx() {
        String report = inner.supportsCredit("bob", 50);
        System.out.println("[PropagationOuterService] 无外层事务, SUPPORTS 非事务执行: " + report);
        return report;
    }

    /**
     * NOT_SUPPORTED：外层事务被挂起，内层 update 自动提交落库；
     * 外层随后用 currentTransactionStatus().setRollbackOnly() 静默回滚自己的修改。
     * 结果：外层的钱没动，内层的钱已经到账——"挂起"就是字面意义的暂停外层事务。
     */
    @Transactional
    public String notSupportedSuspendThenOuterRollsBack() {
        debit("alice", "bob", 100);
        String report = inner.notSupportedCredit("carol", 50);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        System.out.println("[PropagationOuterService] 外层已标记回滚, NOT_SUPPORTED 的修改不受影响");
        return report;
    }

    /** MANDATORY 反例：本方法自己没有事务，内层 MANDATORY 直接拒绝 */
    public void mandatoryWithoutOuterTx() {
        inner.mandatoryPing();
    }

    /** MANDATORY 正例：外层有事务，内层 MANDATORY 正常加入 */
    @Transactional
    public String mandatoryWithOuterTx() {
        return inner.mandatoryPing();
    }

    /** NEVER 反例：外层有事务，内层 NEVER 直接拒绝 */
    @Transactional
    public void neverWithOuterTx() {
        inner.neverPing();
    }

    /** NEVER 正例：没有外层事务，内层 NEVER 正常（非事务）执行 */
    public String neverWithoutOuterTx() {
        return inner.neverPing();
    }

    private void debit(String from, String to, int amount) {
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", amount, from);
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", amount, to);
    }
}
