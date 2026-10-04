package com.alec.InnovateX.spring.transaction;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 事务主题总装配（纯 @Configuration + @Bean，无 XML、无组件扫描、无 Spring Boot）。
 *
 * 设计动机逐条讲清楚：
 * 1. {@link EmbeddedDatabaseBuilder} + H2 内存库：随 JVM 存活、零外部环境，测试完全离线自包含；
 *    addScript("transaction/schema.sql") 在 build() 时建表并灌入 alice/bob/carol/dave 四个初始账户。
 * 2. generateUniqueName(true)：每次 build 生成全局唯一库名——每个测试方法各自 new 一个
 *    AnnotationConfigApplicationContext(TxConfig.class)，拿到互不相干的独立库，
 *    从数据层面保证测试之间零共享、天然不依赖执行顺序。
 * 3. {@link EnableTransactionManagement}：开启注解驱动事务（等价于 XML 时代的
 *    &lt;tx:annotation-driven/&gt;）。它注册的事务切面会自动为带 @Transactional 的 Bean
 *    生成代理——"声明式事务"的全部魔法浓缩在这一个注解上。
 * 4. 事务管理器选 {@link DataSourceTransactionManager}：spring-jdbc 体系的标准实现，
 *    职责是"一个事务管一个 Connection"——begin/commit/rollback，并在开启事务时把
 *    Connection 绑定到当前线程（TransactionSynchronizationManager 的 ThreadLocal），
 *    这正是 JdbcTemplate 能"自动加入当前事务"的底层机制。
 * 5. JdbcTemplate / NamedParameterJdbcTemplate / TransactionTemplate 全部显式声明为 @Bean；
 *    业务 Bean 一律显式构造器注入 + final 字段，不用 lombok、不用字段注入。
 */
@Configuration
@EnableTransactionManagement
public class TxConfig {

    /**
     * 唯一命名的 H2 内存数据源：每个 ApplicationContext 一个独立库。
     * 对比：如果不加 generateUniqueName，所有上下文共用同一个名为 testdb 的内存库，
     * 前一个测试的数据残留会污染后一个测试的断言。
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("transaction/schema.sql")
                .build();
    }

    /** 经典 ? 占位符模板：每次操作内部都走 DataSourceUtils 取"事务绑定的连接" */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** :name 命名占位符模板：参数再多也与顺序无关，基于同一个 DataSource */
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /** 事务管理器：@Transactional 与 TransactionTemplate 的共同底层 */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /** 编程式事务模板（默认 REQUIRED 传播、DEFAULT 隔离），需要定制时可以现场 new 一个 */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    // ---------------- 业务 Bean（全部显式构造器注入，跨 Bean 调用保证经过代理） ----------------

    /** 非事务读：测试断言与日志快照的统一入口 */
    @Bean
    public BalanceQueryService balanceQueryService(JdbcTemplate jdbcTemplate) {
        return new BalanceQueryService(jdbcTemplate);
    }

    /** 声明式事务基础：提交 / RuntimeException 回滚 */
    @Bean
    public TransferService transferService(JdbcTemplate jdbcTemplate) {
        return new TransferService(jdbcTemplate);
    }

    /** 传播行为的"内层"：7 种传播行为各一个方法，必须被别的 Bean 调用才能经过代理 */
    @Bean
    public PropagationInnerService propagationInnerService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return new PropagationInnerService(jdbcTemplate, dataSource);
    }

    /** 传播行为的"外层"：编排内层调用，演示跨 Bean 传播（避免自调用失效） */
    @Bean
    public PropagationOuterService propagationOuterService(JdbcTemplate jdbcTemplate,
                                                            PropagationInnerService propagationInnerService) {
        return new PropagationOuterService(jdbcTemplate, propagationInnerService);
    }

    /** 事务失效四连（自调用 / private / 异常被吞 / 默认只回滚 Runtime）+ rollbackFor 补救 */
    @Bean
    public TxFailureService txFailureService(JdbcTemplate jdbcTemplate) {
        return new TxFailureService(jdbcTemplate);
    }

    /** TransactionTemplate 编程式事务：execute 回调、setRollbackOnly、现场定制传播/隔离 */
    @Bean
    public TemplateTxService templateTxService(JdbcTemplate jdbcTemplate,
                                               TransactionTemplate transactionTemplate,
                                               PlatformTransactionManager transactionManager) {
        return new TemplateTxService(jdbcTemplate, transactionTemplate, transactionManager);
    }

    /** 裸 PlatformTransactionManager：手动 getTransaction / commit / rollback 的最底层形态 */
    @Bean
    public RawTxService rawTxService(JdbcTemplate jdbcTemplate,
                                     PlatformTransactionManager transactionManager) {
        return new RawTxService(jdbcTemplate, transactionManager);
    }

    /** NamedParameterJdbcTemplate 专题：:name 占位符的插入与查询 */
    @Bean
    public NamedParamJdbcService namedParamJdbcService(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new NamedParamJdbcService(namedParameterJdbcTemplate);
    }

    /** JdbcTemplate 细节四件套：batchUpdate / RowMapper / ResultSetExtractor / 异常转译 */
    @Bean
    public JdbcDetailService jdbcDetailService(JdbcTemplate jdbcTemplate) {
        return new JdbcDetailService(jdbcTemplate);
    }

    /** 事务同步器：全生命周期回调 + 事务-连接绑定原理（依赖内层 Bean 触发挂起/恢复） */
    @Bean
    public TxSyncService txSyncService(JdbcTemplate jdbcTemplate,
                                       DataSource dataSource,
                                       PropagationInnerService propagationInnerService) {
        return new TxSyncService(jdbcTemplate, dataSource, propagationInnerService);
    }

    /** 隔离级别探针：@Transactional(isolation=...) 如何落到事务绑定的 Connection 上 */
    @Bean
    public IsolationService isolationService(DataSource dataSource) {
        return new IsolationService(dataSource);
    }

    /** 事务内发布事件（与监听器 Bean 形成"发布方"） */
    @Bean
    public TxEventService txEventService(JdbcTemplate jdbcTemplate, ApplicationEventPublisher publisher) {
        return new TxEventService(jdbcTemplate, publisher);
    }

    /** 事件监听方：@EventListener vs @TransactionalEventListener(AFTER_COMMIT) 的接收时机对比 */
    @Bean
    public TxEventObservers txEventObservers(DataSource dataSource) {
        return new TxEventObservers(dataSource);
    }
}
