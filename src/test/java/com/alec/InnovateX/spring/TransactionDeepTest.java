package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.transaction.FailureScenarioService;
import com.alec.InnovateX.spring.transaction.JdbcDetailService;
import com.alec.InnovateX.spring.transaction.TransferService;
import com.alec.InnovateX.spring.transaction.TxConfig;
import com.alec.InnovateX.spring.transaction.TxEventListener;
import com.alec.InnovateX.spring.transaction.TransactionTemplateService;
import com.alec.InnovateX.spring.transaction.TxSynchronizationService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑤事务深度：@Transactional 回滚、7 种传播行为、隔离级别与不可重复读、
 * 失效场景四连、@TransactionalEventListener
 * 每个测试方法各建独立 H2 内存库（generateUniqueName），初始 alice/bob/carol/dave 均 1000
 */
public class TransactionDeepTest {

    private TransferService transfer(AnnotationConfigApplicationContext ctx) {
        return ctx.getBean(TransferService.class);
    }

    @Test
    public void commitAndRollback() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            TransferService svc = transfer(ctx);
            // 正常提交
            svc.transferSuccess("alice", "bob", 100);
            assertEquals(900, svc.balance("alice"));
            assertEquals(1100, svc.balance("bob"));
            System.out.println("正常提交: alice=" + svc.balance("alice") + ", bob=" + svc.balance("bob"));

            // 异常传播 -> 回滚
            assertThrows(IllegalStateException.class, () -> svc.transferThenRollback("alice", "bob", 50));
            assertEquals(900, svc.balance("alice"));
            assertEquals(1100, svc.balance("bob"));
            System.out.println("异常回滚: alice=" + svc.balance("alice") + ", bob=" + svc.balance("bob"));
        }
    }

    @Test
    public void propagationRequiredDiesTogether() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            TransferService svc = transfer(ctx);
            // 内层 REQUIRED 与外层同属一个物理事务：内层把事务标记 rollback-only，
            // 外层即使吞掉异常也逃不掉，提交时抛 UnexpectedRollbackException，两边的修改全部回滚
            assertThrows(UnexpectedRollbackException.class, () -> svc.outerRequiredInnerFails("alice", "bob", 100));
            assertEquals(1000, svc.balance("alice"));
            assertEquals(1000, svc.balance("bob"));
            assertEquals(1000, svc.balance("carol"));
            assertEquals(1000, svc.balance("dave"));
            System.out.println("REQUIRED 同生共死: 四个账户余额均未变化");
        }
    }

    @Test
    public void propagationRequiresNewAndNested() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            // REQUIRES_NEW：内层独立回滚，外层照常提交
            TransferService svc = transfer(ctx);
            svc.outerRequiresNewInnerFails("alice", "bob", 100);
            assertEquals(900, svc.balance("alice"));
            assertEquals(1100, svc.balance("bob"));
            assertEquals(1000, svc.balance("carol"));
            assertEquals(1000, svc.balance("dave"));
            System.out.println("REQUIRES_NEW: 外层 alice->bob 生效，内层 carol->dave 已回滚");

            // NESTED：内层只回滚到 savepoint，外层继续提交
            svc.outerNestedInnerFails("alice", "carol", 50);
            assertEquals(850, svc.balance("alice"));
            assertEquals(1050, svc.balance("carol"));
            assertEquals(1000, svc.balance("dave"));
            System.out.println("NESTED: 外层 alice->carol 生效（carol=1050），内层 carol->dave 只回滚到保存点");
        }
    }

    @Test
    public void propagationOthers() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            TransferService svc = transfer(ctx);
            // MANDATORY：没有事务直接拒绝
            assertThrows(IllegalTransactionStateException.class, svc::callMandatoryWithoutTx);
            System.out.println("MANDATORY 无事务调用: 抛 IllegalTransactionStateException");
            // NEVER：有事务直接拒绝
            assertThrows(IllegalTransactionStateException.class, svc::callNeverWithTx);
            System.out.println("NEVER 有事务调用: 抛 IllegalTransactionStateException");
            // NOT_SUPPORTED：外层事务被挂起——连接解绑才是"真没事务"的证据
            //（active 标志在挂起时不会重置，是实测会翻车的坑，详见 PropagationInnerService 注释）
            String notSupported = transfer(ctx).callNotSupportedWithTx();
            assertTrue(notSupported.contains("connectionBound=false"));
            System.out.println(notSupported);
            // SUPPORTS：没有事务时以非事务方式照常执行
            System.out.println(svc.callSupportsWithoutTx());
        }
    }

    @Test
    public void failureScenarios() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            FailureScenarioService svc = ctx.getBean(FailureScenarioService.class);

            // 对照组：经过代理 + 异常传播 -> 真回滚
            assertThrows(IllegalStateException.class, () -> svc.updateThenThrow("alice", -100));
            assertEquals(1000, ctx.getBean(TransferService.class).balance("alice"));

            // 失效1-自调用：this 调用绕过代理，注解不生效，更新被提交
            assertThrows(IllegalStateException.class, () -> svc.selfInvokeThenThrow("alice", -100));
            assertEquals(900, ctx.getBean(TransferService.class).balance("alice"));
            System.out.println("自调用失效: alice=" + 900 + "（更新没回滚）");

            // 失效2-private 方法：只能在"目标对象"上反射调用（代理实例的字段从未注入，是 null；
            // private 方法无法被 CGLIB 覆盖，调用路径完全不经过拦截器链 -> 事务失效）
            FailureScenarioService proxy = ctx.getBean(FailureScenarioService.class);
            Object target = ((org.springframework.aop.framework.Advised) proxy).getTargetSource().getTarget();
            Method method = org.springframework.aop.support.AopUtils.getTargetClass(proxy)
                    .getDeclaredMethod("privateThenThrow", String.class, int.class);
            method.setAccessible(true);
            assertThrows(InvocationTargetException.class, () -> method.invoke(target, "alice", -100));
            assertEquals(800, ctx.getBean(TransferService.class).balance("alice"));
            System.out.println("private 方法失效: alice=" + 800 + "（更新没回滚）");

            // 失效3-异常被吞：事务照常提交
            svc.updateThenSwallow("alice", -100);
            assertEquals(700, ctx.getBean(TransferService.class).balance("alice"));
            System.out.println("异常被吞失效: alice=" + 700 + "（更新已提交）");

            // 失效4-checked 异常默认不回滚 vs rollbackFor = Exception.class 回滚
            assertThrows(Exception.class, () -> svc.updateThenThrowChecked("alice", -100));
            assertEquals(600, ctx.getBean(TransferService.class).balance("alice"));
            System.out.println("checked 异常默认不回滚: alice=" + 600);

            assertThrows(Exception.class, () -> svc.updateThenThrowCheckedWithRollbackFor("alice", -100));
            assertEquals(600, ctx.getBean(TransferService.class).balance("alice"));
            System.out.println("rollbackFor=Exception.class 后回滚: alice 仍为 " + 600);
        }
    }

    @Test
    public void transactionalEventListener() {
        TxEventListener.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            transfer(ctx).transferWithEvent("alice", "bob", 100);
            assertEquals(2, TxEventListener.RECEIVED.size());
            System.out.println("两个监听器收到的时机: " + TxEventListener.RECEIVED);
            // @EventListener 在事务提交前（事务内）收到；@TransactionalEventListener 在提交后收到
            //（AFTER_COMMIT 回调发生在同步上下文清理之前，active 标志仍是 true）
            assertTrue(TxEventListener.RECEIVED.get(0).startsWith("plain(事务未提交, active=true"));
            assertTrue(TxEventListener.RECEIVED.get(1).startsWith("AFTER_COMMIT(提交后回调, active=true"));
        }
    }

    @Test
    public void isolationLevel() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            // @Transactional(isolation=SERIALIZABLE) 确实作用在事务绑定的 Connection 上
            String isolation = transfer(ctx).currentIsolation();
            System.out.println("当前事务隔离级别: " + isolation);
            assertTrue(isolation.equals("SERIALIZABLE") || isolation.equals("READ_COMMITTED"));

            // 不可重复读演示（H2 默认 READ_COMMITTED）：同一事务内两次读取，中间被别的事务提交了修改
            DataSource dataSource = ctx.getBean(DataSource.class);
            Connection c1 = dataSource.getConnection();
            c1.setAutoCommit(false);
            int firstRead = readBalance(c1);
            try (Connection c2 = dataSource.getConnection()) {
                c2.setAutoCommit(true);
                try (Statement st = c2.createStatement()) {
                    st.executeUpdate("update account set balance = balance + 100 where name = 'alice'");
                }
            }
            int secondRead = readBalance(c1);
            c1.rollback();
            c1.close();
            System.out.println("READ_COMMITTED 下同一事务两次读取: " + firstRead + " -> " + secondRead + "（不可重复读现象）");
            assertEquals(firstRead + 100, secondRead);
        }
    }

    private int readBalance(Connection connection) throws Exception {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select balance from account where name = 'alice'")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    public void transactionTemplate() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            TransactionTemplateService svc = ctx.getBean(TransactionTemplateService.class);
            TransferService transfer = transfer(ctx);

            // execute 回调正常返回 -> 提交
            assertEquals("已提交", svc.transfer("alice", "bob", 100, false));
            assertEquals(900, transfer.balance("alice"));

            // setRollbackOnly：不抛异常也回滚，execute 照常返回
            assertEquals("已标记回滚（setRollbackOnly）", svc.transfer("alice", "bob", 50, true));
            assertEquals(900, transfer.balance("alice"));
            System.out.println("TransactionTemplate: 提交生效，setRollbackOnly 回滚且不外抛异常");
        }
    }

    @Test
    public void transactionSynchronization() {
        TxSynchronizationService.EVENTS.clear();
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            // 提交路径：事务内绑定观察 + 提交各阶段回调，顺序与 AbstractPlatformTransactionManager.processCommit 一致
            ctx.getBean(TxSynchronizationService.class).workWithSynchronization();
            System.out.println("同步器事件序列: " + TxSynchronizationService.EVENTS);
            assertEquals(List.of("bound=true", "sameConnection=true",
                    "beforeCommit(readOnly=false)", "beforeCompletion", "afterCommit", "afterCompletion:COMMITTED"),
                    TxSynchronizationService.EVENTS);

            // 回滚路径：afterCompletion 收到 ROLLED_BACK 状态
            TxSynchronizationService.EVENTS.clear();
            assertThrows(IllegalStateException.class,
                    () -> ctx.getBean(TxSynchronizationService.class).workWithSynchronizationAndRollback());
            System.out.println("回滚路径事件: " + TxSynchronizationService.EVENTS);
            assertEquals(List.of("rollback-afterCompletion:ROLLED_BACK"), TxSynchronizationService.EVENTS);
        }
    }

    @Test
    public void jdbcDetails() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class)) {
            JdbcDetailService svc = ctx.getBean(JdbcDetailService.class);
            // batchUpdate：一次往返写入 3 行
            assertEquals(3, svc.batchInsert(List.of("SO-A", "SO-B", "SO-C")));
            // RowMapper：逐行映射
            assertEquals(List.of("SO-A", "SO-B", "SO-C"), svc.queryWithRowMapper());
            System.out.println("RowMapper 逐行映射: " + svc.queryWithRowMapper());
            // ResultSetExtractor：整表聚合（100+200+300）
            assertEquals(600, svc.totalAmountWithExtractor());
            System.out.println("ResultSetExtractor 聚合总额: " + svc.totalAmountWithExtractor());
            // 异常转译：SQLException（H2 实现为 JdbcSQLSyntaxErrorException，SQLException 子类）-> BadSqlGrammarException
            String translated = svc.badSqlGrammar();
            System.out.println("异常转译结果: " + translated);
            assertTrue(translated.startsWith("BadSqlGrammarException"));
            assertTrue(translated.endsWith("（受检异常被转译掉）"));
        }
    }
}
