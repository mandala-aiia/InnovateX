package com.alec.InnovateX.spring.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 事务演示配置：
 * - H2 内存库 + EmbeddedDatabaseBuilder，启动即执行 schema.sql，完全自包含
 * - generateUniqueName：每个 ApplicationContext 一个独立命名的库，测试之间互不干扰
 * - @EnableTransactionManagement 开启注解事务（XML 等价物是 <tx:annotation-driven/>）
 */
@Configuration
@EnableTransactionManagement
public class TxConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("transaction/schema.sql")
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PropagationInnerService propagationInnerService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return new PropagationInnerService(jdbcTemplate, dataSource);
    }

    @Bean
    public TransferService transferService(JdbcTemplate jdbcTemplate, PropagationInnerService inner,
                                           org.springframework.context.ApplicationEventPublisher publisher) {
        return new TransferService(jdbcTemplate, inner, publisher);
    }

    @Bean
    public FailureScenarioService failureScenarioService(JdbcTemplate jdbcTemplate) {
        return new FailureScenarioService(jdbcTemplate);
    }

    @Bean
    public TxSynchronizationService txSynchronizationService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return new TxSynchronizationService(jdbcTemplate, dataSource);
    }

    @Bean
    public JdbcDetailService jdbcDetailService(JdbcTemplate jdbcTemplate) {
        return new JdbcDetailService(jdbcTemplate);
    }

    /** TransactionTemplate：编程式事务的现代封装（传播行为/隔离级别可编程配置） */
    @Bean
    public org.springframework.transaction.support.TransactionTemplate transactionTemplate(
            PlatformTransactionManager transactionManager) {
        return new org.springframework.transaction.support.TransactionTemplate(transactionManager);
    }

    @Bean
    public TransactionTemplateService transactionTemplateService(JdbcTemplate jdbcTemplate,
                                                                 org.springframework.transaction.support.TransactionTemplate transactionTemplate) {
        return new TransactionTemplateService(jdbcTemplate, transactionTemplate);
    }

    @Bean
    public TxEventListener txEventListener() {
        return new TxEventListener();
    }
}
