package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务失效四连 + rollbackFor 补救。每个场景都以"修改是否落库"为铁证：
 *
 * 1. 自调用失效：外层方法【没有】@Transactional，内部 this.xxx() 调用带注解的方法——
 *    this 引用的是目标对象而不是代理，调用不经过拦截器链，注解形同虚设，异常抛了也照样提交。
 * 2. private 方法失效：Spring 用 CGLIB"生成子类并覆盖方法"来织入事务切面，
 *    private 方法无法被覆盖；AnnotationTransactionAttributeSource 默认也只读 public 方法的注解。
 *    测试通过 Advised.getTargetSource() 拿到原始目标对象反射调用，证明即使直接调目标对象也没有事务。
 * 3. 异常被 catch 吞掉失效：事务切面只认"传播出方法边界"的异常；
 *    方法内部自己 catch 掉，切面看到的是正常返回，于是照常提交。
 * 4. 默认回滚规则只认 RuntimeException/Error：受检异常默认【不】回滚——
 *    想让受检异常也回滚，用 @Transactional(rollbackFor = Exception.class) 显式改规则。
 */
public class TxFailureService {

    private final JdbcTemplate jdbcTemplate;

    public TxFailureService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 生效对照组：经代理调用 + RuntimeException 传播出边界 -> 真回滚 */
    @Transactional
    public void updateThenThrow(String name, int delta) {
        update(name, delta);
        throw new IllegalStateException("经代理传播出去的异常 -> 回滚");
    }

    /** 失效1-自调用：外层无事务注解，this 调用绕过代理，update 落库、异常白抛 */
    public void selfInvokeThenThrow(String name, int delta) {
        System.out.println("[TxFailureService] 自调用前外层事务状态 active="
                + TransactionSynchronizationManager.isActualTransactionActive() + "（外层根本没开事务）");
        this.updateThenThrow(name, delta);
    }

    /**
     * 失效2-private 方法：CGLIB 无法覆盖 private，注解不生效。
     * 正常代码不会调用它，测试用反射在"原始目标对象"上调用以证明其无事务。
     */
    @Transactional
    private void privateUpdateThenThrow(String name, int delta) {
        update(name, delta);
        System.out.println("[TxFailureService] private 方法内 active="
                + TransactionSynchronizationManager.isActualTransactionActive() + "（false=注解没生效）");
        throw new IllegalStateException("private 方法里抛异常也不会触发回滚");
    }

    /** 失效3-异常被吞：切面感知不到异常，按正常返回提交 */
    @Transactional
    public void updateAndSwallow(String name, int delta) {
        update(name, delta);
        try {
            throw new IllegalStateException("被方法内部 catch 掉的异常");
        } catch (IllegalStateException e) {
            System.out.println("[TxFailureService] 异常被吞: " + e.getMessage() + " -> 事务照常提交");
        }
    }

    /** 失效4-受检异常：默认回滚规则不认识 Exception，事务照常提交 */
    @Transactional
    public void updateThenThrowChecked(String name, int delta) throws Exception {
        update(name, delta);
        throw new Exception("受检异常默认不回滚 -> 修改落库");
    }

    /** rollbackFor 补救：同样抛受检异常，这次按显式配置的规则回滚 */
    @Transactional(rollbackFor = Exception.class)
    public void updateThenThrowCheckedWithRollbackFor(String name, int delta) throws Exception {
        update(name, delta);
        throw new Exception("配置 rollbackFor=Exception.class 后 -> 受检异常也回滚");
    }

    private void update(String name, int delta) {
        jdbcTemplate.update("update account set balance = balance + ? where name = ?", delta, name);
    }
}
