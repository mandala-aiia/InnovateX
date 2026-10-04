package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.transaction.BalanceQueryService;
import com.alec.InnovateX.spring.transaction.IsolationService;
import com.alec.InnovateX.spring.transaction.JdbcDetailService;
import com.alec.InnovateX.spring.transaction.NamedParamJdbcService;
import com.alec.InnovateX.spring.transaction.PropagationOuterService;
import com.alec.InnovateX.spring.transaction.RawTxService;
import com.alec.InnovateX.spring.transaction.TemplateTxService;
import com.alec.InnovateX.spring.transaction.TransferService;
import com.alec.InnovateX.spring.transaction.TxConfig;
import com.alec.InnovateX.spring.transaction.TxEventObservers;
import com.alec.InnovateX.spring.transaction.TxEventService;
import com.alec.InnovateX.spring.transaction.TxFailureService;
import com.alec.InnovateX.spring.transaction.TxSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
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
 * 主题：事务深度 + spring-jdbc（全部基于 H2 内存库，完全自包含、离线可测）。
 *
 * 每个测试方法各自 new 一个 AnnotationConfigApplicationContext(TxConfig.class)，
 * 配合 generateUniqueName(true) 拿到独立内存库（初始 alice/bob/carol/dave 均 1000），
 * 测试之间零共享状态，执行顺序随意。
 * 静态观察列表（TxSyncService.EVENTS / TxEventObservers.RECEIVED）在使用前先清空，
 * 同样保证不依赖执行顺序。
 */
public class TransactionDeepTest {

    /** 每个测试自建上下文（try-with-resources 保证关闭并释放内存库） */
    private static AnnotationConfigApplicationContext load() {
        return new AnnotationConfigApplicationContext(TxConfig.class);
    }

    /** 断言统一出口：非事务读（已提交视图） */
    private static BalanceQueryService balances(AnnotationConfigApplicationContext ctx) {
        return ctx.getBean(BalanceQueryService.class);
    }

    // ---------------- 声明式事务基础 ----------------

    /** @Transactional 基础：正常返回提交、RuntimeException 传播回滚（余额断言） */
    @Test
    public void declarativeCommitAndRollback() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            TransferService svc = ctx.getBean(TransferService.class);
            BalanceQueryService q = balances(ctx);

            // 正常路径：方法返回即提交
            svc.transferCommit("alice", "bob", 100);
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));

            // 异常路径：RuntimeException 出边界 -> 整体回滚
            assertThrows(IllegalStateException.class, () -> svc.transferRollbackOnRuntime("alice", "bob", 50));
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
            System.out.println("[Test] 声明式提交/回滚: " + q.snapshot());
        }
    }

    // ---------------- 七种传播行为（跨 Bean 调用） ----------------

    /** REQUIRED：内层加入外层一起提交；内层抛异常被外层吞掉 -> 共享事务 rollback-only，提交时抛 UnexpectedRollbackException */
    @Test
    public void propagationRequiredJoinAndDieTogether() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            PropagationOuterService outer = ctx.getBean(PropagationOuterService.class);
            BalanceQueryService q = balances(ctx);

            // 正向：外层 alice->bob 60，内层 REQUIRED 加入 carol->dave 40，同事务一起提交
            outer.requiredJoinAndCommitTogether();
            assertEquals(940, q.balance("alice"));
            assertEquals(1060, q.balance("bob"));
            assertEquals(960, q.balance("carol"));
            assertEquals(1040, q.balance("dave"));

            // 反向：内层异常把共享事务标记 rollback-only，外层吞异常也逃不掉
            assertThrows(UnexpectedRollbackException.class, outer::requiredInnerFailsOuterSwallows);
            assertEquals(940, q.balance("alice"));
            assertEquals(1060, q.balance("bob"));
            assertEquals(960, q.balance("carol"));
            assertEquals(1040, q.balance("dave"));
            System.out.println("[Test] REQUIRED 加入即同生共死: " + q.snapshot());
        }
    }

    /** REQUIRES_NEW 双向验证：内层失败只回滚自己；外层失败也带不走已提交的内层 */
    @Test
    public void propagationRequiresNewIndependentBothDirections() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            PropagationOuterService outer = ctx.getBean(PropagationOuterService.class);
            BalanceQueryService q = balances(ctx);

            // 方向一：内层 REQUIRES_NEW 抛异常 -> 只回滚独立事务，外层照常提交
            outer.requiresNewInnerFailsOuterCommits();
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
            assertEquals(1000, q.balance("carol"));
            assertEquals(1000, q.balance("dave"));

            // 方向二：内层独立事务先提交，随后外层失败回滚 -> 内层修改存活
            assertThrows(IllegalStateException.class, outer::requiresNewInnerCommitsOuterFails);
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
            assertEquals(950, q.balance("carol"));
            assertEquals(1050, q.balance("dave"));
            System.out.println("[Test] REQUIRES_NEW 独立事务: " + q.snapshot());
        }
    }

    /** NESTED：内层回滚只到 savepoint，外层捕获异常后继续提交自己的修改 */
    @Test
    public void propagationNestedSavepoint() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            PropagationOuterService outer = ctx.getBean(PropagationOuterService.class);
            BalanceQueryService q = balances(ctx);

            outer.nestedInnerFailsOuterCommits();
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
            assertEquals(1000, q.balance("carol"));
            assertEquals(1000, q.balance("dave"));
            System.out.println("[Test] NESTED 保存点: 外层 alice->bob 已提交, 内层 carol->dave 已回滚到 savepoint");
        }
    }

    /** SUPPORTS：有事务就加入（随外层一起回滚），没事务就非事务执行（自动提交落库） */
    @Test
    public void propagationSupportsJoinOrBare() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            PropagationOuterService outer = ctx.getBean(PropagationOuterService.class);
            BalanceQueryService q = balances(ctx);

            // 有外层事务：SUPPORTS 加入，外层 setRollbackOnly -> 加入的修改一并回滚
            String joinReport = outer.supportsJoinThenOuterRollsBack();
            assertTrue(joinReport.contains("active=true"), joinReport);
            assertEquals(1000, q.balance("alice"));

            // 无外层事务：SUPPORTS 以非事务方式执行，update 自动提交落库
            String bareReport = outer.supportsWithoutOuterTx();
            assertTrue(bareReport.contains("active=false"), bareReport);
            assertEquals(1050, q.balance("bob"));
            System.out.println("[Test] SUPPORTS 随遇而安: join=" + joinReport + ", bare=" + bareReport);
        }
    }

    /** NOT_SUPPORTED：外层事务被挂起，内层 update 自动提交；外层随后静默回滚自己的修改 */
    @Test
    public void propagationNotSupportedSuspendsOuter() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            PropagationOuterService outer = ctx.getBean(PropagationOuterService.class);
            BalanceQueryService q = balances(ctx);

            String report = outer.notSupportedSuspendThenOuterRollsBack();
            // 连接解绑才是"真没事务"的证据（active 标志挂起时不重置，可能是残留 true）
            assertTrue(report.contains("connectionBound=false"), report);
            assertEquals(1000, q.balance("alice"));   // 外层回滚
            assertEquals(1000, q.balance("bob"));
            assertEquals(1050, q.balance("carol"));   // 内层自动提交存活
            System.out.println("[Test] NOT_SUPPORTED 挂起: " + report + " -> " + q.snapshot());
        }
    }

    /** MANDATORY 无事务抛异常 / NEVER 有事务抛异常（IllegalTransactionStateException），正反两个正例 */
    @Test
    public void propagationMandatoryAndNever() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            PropagationOuterService outer = ctx.getBean(PropagationOuterService.class);

            // MANDATORY：调用方没有事务 -> 拒绝
            assertThrows(IllegalTransactionStateException.class, outer::mandatoryWithoutOuterTx);
            // MANDATORY 正例：调用方有事务 -> 正常加入
            assertTrue(outer.mandatoryWithOuterTx().contains("active=true"));
            // NEVER：调用方有事务 -> 拒绝
            assertThrows(IllegalTransactionStateException.class, outer::neverWithOuterTx);
            // NEVER 正例：调用方没有事务 -> 正常（非事务）执行
            assertTrue(outer.neverWithoutOuterTx().contains("active=false"));
            System.out.println("[Test] MANDATORY/NEVER 四象限验证通过");
        }
    }

    // ---------------- 事务失效四连 + rollbackFor ----------------

    /** 失效四连（自调用/private/异常被吞/默认只回滚 Runtime）各以余额落库为铁证，最后 rollbackFor 补救 */
    @Test
    public void txFailureFourScenariosAndRollbackFor() throws Exception {
        try (AnnotationConfigApplicationContext ctx = load()) {
            TxFailureService svc = ctx.getBean(TxFailureService.class);
            BalanceQueryService q = balances(ctx);

            // 对照组：经代理 + 异常传播 -> 真回滚
            assertThrows(IllegalStateException.class, () -> svc.updateThenThrow("alice", 100));
            assertEquals(1000, q.balance("alice"));

            // 失效1-自调用：this 调用绕过代理，异常白抛、修改落库
            assertThrows(IllegalStateException.class, () -> svc.selfInvokeThenThrow("alice", 100));
            assertEquals(1100, q.balance("alice"));

            // 失效2-private 方法：在原始目标对象上反射调用（代理根本无法覆盖 private），
            // 调用路径不经过拦截器链 -> 注解彻底失效，修改落库
            TxFailureService proxy = ctx.getBean(TxFailureService.class);
            Object target = ((Advised) proxy).getTargetSource().getTarget();
            Method privateMethod = AopUtils.getTargetClass(proxy)
                    .getDeclaredMethod("privateUpdateThenThrow", String.class, int.class);
            privateMethod.setAccessible(true);
            assertThrows(InvocationTargetException.class, () -> privateMethod.invoke(target, "alice", 100));
            assertEquals(1200, q.balance("alice"));

            // 失效3-异常被 catch 吞掉：切面看到"正常返回"，照常提交
            svc.updateAndSwallow("alice", 100);
            assertEquals(1300, q.balance("alice"));

            // 失效4-受检异常：默认回滚规则只认 RuntimeException/Error -> 照常提交
            assertThrows(Exception.class, () -> svc.updateThenThrowChecked("alice", 100));
            assertEquals(1400, q.balance("alice"));

            // 补救：rollbackFor = Exception.class 后，同样的受检异常触发回滚
            assertThrows(Exception.class, () -> svc.updateThenThrowCheckedWithRollbackFor("alice", 100));
            assertEquals(1400, q.balance("alice"));
            System.out.println("[Test] 失效四连+rollbackFor: " + q.snapshot());
        }
    }

    // ---------------- 编程式事务 ----------------

    /** TransactionTemplate 基础：execute 正常返回提交；setRollbackOnly 不抛异常也回滚 */
    @Test
    public void transactionTemplateExecuteAndRollbackOnly() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            TemplateTxService svc = ctx.getBean(TemplateTxService.class);
            BalanceQueryService q = balances(ctx);

            assertEquals("committed", svc.transferViaExecute("alice", "bob", 100));
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));

            // setRollbackOnly：方法正常返回、无异常外抛，但修改回滚
            assertEquals("rollback-only", svc.transferThenSilentRollback("alice", "bob", 50));
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
            System.out.println("[Test] TransactionTemplate: " + q.snapshot());
        }
    }

    /** TransactionTemplate 编程式配置：现场 new 的 REQUIRES_NEW + REPEATABLE_READ 模板，外层失败内层存活 */
    @Test
    public void transactionTemplateProgrammaticPropagationAndIsolation() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            TemplateTxService svc = ctx.getBean(TemplateTxService.class);
            BalanceQueryService q = balances(ctx);

            String result = svc.requiresNewWithCustomIsolationInsideFailingOuter("carol", "dave", 50);
            // 回调里读到的隔离级别 = 编程设置的 ISOLATION_REPEATABLE_READ
            assertEquals("innerIsolation=" + TransactionDefinition.ISOLATION_REPEATABLE_READ, result);
            assertEquals(1000, q.balance("alice"));   // 外层模板回滚
            assertEquals(1000, q.balance("bob"));
            assertEquals(950, q.balance("carol"));    // 内层独立事务存活
            assertEquals(1050, q.balance("dave"));
            System.out.println("[Test] 模板编程式传播+隔离: " + result + " -> " + q.snapshot());
        }
    }

    /** 裸 PlatformTransactionManager：手动 getTransaction/commit/rollback，提交后余额变化、回滚后余额不变 */
    @Test
    public void rawPlatformTransactionManagerCommitRollback() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            RawTxService svc = ctx.getBean(RawTxService.class);
            BalanceQueryService q = balances(ctx);

            // 手动提交：一借一贷两行落库
            assertEquals(2, svc.manuallyCommittedTransfer("alice", "bob", 100));
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));

            // 手动回滚：扣款 SQL 已执行但不落库
            assertEquals(1, svc.manuallyRolledBackDeduction("alice", 200));
            assertEquals(900, q.balance("alice"));
            System.out.println("[Test] 裸 PTM 手动 commit/rollback: " + q.snapshot());
        }
    }

    // ---------------- spring-jdbc 细节 ----------------

    /** NamedParameterJdbcTemplate：:name 命名占位符插入与查询 */
    @Test
    public void namedParameterJdbcInsertAndQuery() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            NamedParamJdbcService svc = ctx.getBean(NamedParamJdbcService.class);

            assertEquals(1, svc.insertOrder("SO-001", 100));
            assertEquals(1, svc.insertOrder("SO-002", 250));
            assertEquals(1, svc.insertOrder("SO-003", 400));

            assertEquals(250, svc.amountOfOrder("SO-002"));
            assertEquals(List.of("SO-002", "SO-003"), svc.orderNosAbove(200));
            System.out.println("[Test] 命名参数插入/查询通过, orderNosAbove(200)=" + svc.orderNosAbove(200));
        }
    }

    /** JdbcTemplate 四件套：batchUpdate / RowMapper / ResultSetExtractor / SQLException 异常转译 */
    @Test
    public void jdbcBatchRowMapperExtractorExceptionTranslation() {
        try (AnnotationConfigApplicationContext ctx = load()) {
            JdbcDetailService svc = ctx.getBean(JdbcDetailService.class);

            // 1) batchUpdate：一次往返写入 3 行
            assertEquals(3, svc.insertOrderBatch(List.of("B-01", "B-02", "B-03")));

            // 2) RowMapper：逐行回调映射成对象
            List<JdbcDetailService.OrderRow> orders = svc.listAllOrders();
            assertEquals(3, orders.size());
            assertEquals("B-01", orders.get(0).getOrderNo());
            assertEquals(100, orders.get(0).getAmount());
            assertEquals(300, orders.get(2).getAmount());

            // 3) ResultSetExtractor：整表聚合（100+200+300）
            assertEquals(600, svc.sumAllAmounts());

            // 4) 异常转译：受检 SQLException -> 非受检 BadSqlGrammarException
            String translated = svc.translateBadSql();
            System.out.println("[Test] 异常转译: " + translated);
            assertTrue(translated.startsWith("BadSqlGrammarException"), translated);
            assertTrue(translated.endsWith("根因是SQLException=true"), translated);
        }
    }

    // ---------------- 事务同步器 ----------------

    /** TransactionSynchronization 全阶段回调顺序 + 事务-连接绑定（同事务同一 Connection） */
    @Test
    public void transactionSynchronizationLifecycle() {
        TxSyncService.EVENTS.clear();
        try (AnnotationConfigApplicationContext ctx = load()) {
            BalanceQueryService q = balances(ctx);

            // 提交路径：绑定检查 -> 嵌套 REQUIRES_NEW 触发 suspend/resume -> 提交四阶段
            ctx.getBean(TxSyncService.class).commitPathFullLifecycle();
            System.out.println("[Test] 同步器提交路径事件: " + TxSyncService.EVENTS);
            assertEquals(List.of(
                    "bound=true",
                    "sameConnection=true",
                    "suspend",
                    "resume",
                    "beforeCommit(readOnly=false)",
                    "beforeCompletion",
                    "afterCommit",
                    "afterCompletion:COMMITTED"), TxSyncService.EVENTS);
            // 外层提交 + 内层独立提交同时成立
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
            assertEquals(950, q.balance("carol"));
            assertEquals(1050, q.balance("dave"));

            // 回滚路径：无 afterCommit，afterCompletion 收到 ROLLED_BACK
            TxSyncService.EVENTS.clear();
            assertThrows(IllegalStateException.class,
                    () -> ctx.getBean(TxSyncService.class).rollbackPathLifecycle());
            System.out.println("[Test] 同步器回滚路径事件: " + TxSyncService.EVENTS);
            assertEquals(List.of("beforeCompletion", "afterCompletion:ROLLED_BACK"), TxSyncService.EVENTS);
            assertEquals(900, q.balance("alice"));   // +999 没有落库
        }
    }

    // ---------------- 事务事件 ----------------

    /** @EventListener 提交前收到（读不到未提交转账），@TransactionalEventListener(AFTER_COMMIT) 提交后收到 */
    @Test
    public void eventListenerVsTransactionalEventListenerTiming() {
        TxEventObservers.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = load()) {
            BalanceQueryService q = balances(ctx);

            ctx.getBean(TxEventService.class).transferAndPublish("alice", "bob", 100);

            assertEquals(3, TxEventObservers.RECEIVED.size());
            // 普通 @EventListener：事务尚未提交，另一条连接只能读到旧余额 1000
            assertTrue(TxEventObservers.RECEIVED.get(0).startsWith("immediate(提交前"), TxEventObservers.RECEIVED.get(0));
            assertTrue(TxEventObservers.RECEIVED.get(0).contains("已提交视图余额=1000"), TxEventObservers.RECEIVED.get(0));
            // BEFORE_COMMIT：提交前最后关口，修改已执行完但尚未落库——读到的仍是 1000
            assertTrue(TxEventObservers.RECEIVED.get(1).startsWith("beforeCommit"), TxEventObservers.RECEIVED.get(1));
            assertTrue(TxEventObservers.RECEIVED.get(1).contains("已提交视图余额=1000"), TxEventObservers.RECEIVED.get(1));
            // AFTER_COMMIT：提交后才回调，读到的已是新余额 1100（active 残留 true 属正常清理时序）
            assertTrue(TxEventObservers.RECEIVED.get(2).startsWith("afterCommit(提交后"), TxEventObservers.RECEIVED.get(2));
            assertTrue(TxEventObservers.RECEIVED.get(2).contains("已提交视图余额=1100"), TxEventObservers.RECEIVED.get(2));
            System.out.println("[Test] 两种监听器接收时机: " + TxEventObservers.RECEIVED);
            assertEquals(900, q.balance("alice"));
            assertEquals(1100, q.balance("bob"));
        }
    }

    /** 无事务发布：AFTER_COMMIT 监听器默认 fallbackExecution=false，被静默跳过 */
    @Test
    public void transactionalEventListenerSkippedOutsideTx() {
        TxEventObservers.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = load()) {
            ctx.getBean(TxEventService.class).publishOutsideTransaction();
            assertEquals(1, TxEventObservers.RECEIVED.size());
            assertTrue(TxEventObservers.RECEIVED.get(0).startsWith("immediate"), TxEventObservers.RECEIVED.get(0));
            System.out.println("[Test] 无事务发布只有普通监听器收到: " + TxEventObservers.RECEIVED);
        }
    }

    /** 回滚路径：只有 AFTER_ROLLBACK 收到，BEFORE_COMMIT/AFTER_COMMIT 全部静默跳过，数据库分文未动 */
    @Test
    public void transactionalEventListenerRollbackPhase() {
        TxEventObservers.RECEIVED.clear();
        try (AnnotationConfigApplicationContext ctx = load()) {
            BalanceQueryService q = balances(ctx);
            assertThrows(IllegalStateException.class,
                    () -> ctx.getBean(TxEventService.class).transferAndPublishThenRollback("carol", "dave", 50));

            assertTrue(TxEventObservers.RECEIVED.stream().anyMatch(s -> s.startsWith("immediate")),
                    "普通监听器发布即达: " + TxEventObservers.RECEIVED);
            assertTrue(TxEventObservers.RECEIVED.stream().anyMatch(s -> s.startsWith("afterRollback")),
                    "回滚路径应收到 AFTER_ROLLBACK: " + TxEventObservers.RECEIVED);
            assertTrue(TxEventObservers.RECEIVED.stream().noneMatch(s -> s.startsWith("beforeCommit")),
                    "回滚路径不应出现 BEFORE_COMMIT: " + TxEventObservers.RECEIVED);
            assertTrue(TxEventObservers.RECEIVED.stream().noneMatch(s -> s.startsWith("afterCommit")),
                    "回滚路径不应出现 AFTER_COMMIT: " + TxEventObservers.RECEIVED);
            // 事务回滚：转账未落库
            assertEquals(1000, q.balance("carol"));
            assertEquals(1000, q.balance("dave"));
            System.out.println("[Test] 回滚路径监听: " + TxEventObservers.RECEIVED);
        }
    }

    // ---------------- 隔离级别 ----------------

    /** 两条手动 Connection 对照：READ_COMMITTED 能看到别人的提交（不可重复读），REPEATABLE_READ 快照两次一致 */
    @Test
    public void isolationReadCommittedVsRepeatableRead() throws Exception {
        try (AnnotationConfigApplicationContext ctx = load()) {
            DataSource dataSource = ctx.getBean(DataSource.class);

            // 场景 A：READ_COMMITTED——同一事务内两次读，中间别人提交了 -> 第二次读到新值（不可重复读）
            try (Connection reader = dataSource.getConnection()) {
                reader.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                reader.setAutoCommit(false);
                int firstRead = readAlice(reader);
                commitExternalPlus(dataSource, 100);
                int secondRead = readAlice(reader);
                reader.rollback();
                System.out.println("[Test] READ_COMMITTED 两次读: " + firstRead + " -> " + secondRead + "（不可重复读）");
                assertEquals(firstRead + 100, secondRead);
            }

            // 场景 B：REPEATABLE_READ——同一事务快照，两次读完全一致
            try (Connection reader = dataSource.getConnection()) {
                reader.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                reader.setAutoCommit(false);
                int firstRead = readAlice(reader);
                commitExternalPlus(dataSource, 50);
                int secondRead = readAlice(reader);
                reader.rollback();
                System.out.println("[Test] REPEATABLE_READ 两次读: " + firstRead + " -> " + secondRead + "（快照一致）");
                assertEquals(firstRead, secondRead);
            }
            System.out.println("[Test] 隔离对照结束: " + balances(ctx).snapshot());
        }
    }

    /** @Transactional(isolation=REPEATABLE_READ) 确实 set 到了事务绑定的 Connection 上 */
    @Test
    public void annotationIsolationReachesBoundConnection() throws Exception {
        try (AnnotationConfigApplicationContext ctx = load()) {
            String isolation = ctx.getBean(IsolationService.class).boundConnectionIsolation();
            System.out.println("[Test] 注解隔离级别落到绑定连接: " + isolation);
            assertEquals("REPEATABLE_READ", isolation);
        }
    }

    /** reader 连接读 alice 余额 */
    private int readAlice(Connection reader) throws Exception {
        try (Statement st = reader.createStatement();
             ResultSet rs = st.executeQuery("select balance from account where name = 'alice'")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** 另一条自动提交连接给 alice 加钱并立即提交（模拟"别的事务"） */
    private void commitExternalPlus(DataSource dataSource, int delta) throws Exception {
        try (Connection writer = dataSource.getConnection();
             Statement st = writer.createStatement()) {
            st.executeUpdate("update account set balance = balance + " + delta + " where name = 'alice'");
        }
    }
}
