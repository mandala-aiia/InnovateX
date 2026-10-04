package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 隔离级别探针：证明 @Transactional(isolation=...) 不是摆设——
 * 事务开启时 DataSourceTransactionManager 会把注解里的级别 set 到
 * "事务绑定的那条 Connection"上（doBegin 阶段，先于任何 SQL）。
 *
 * 隔离级别的语义对比（同事务两次读是否一致、能否看见别人已提交的修改）
 * 在测试里用两条手动 Connection 做，保证断言完全确定。
 */
public class IsolationService {

    private final DataSource dataSource;

    public IsolationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** 在 REPEATABLE_READ 事务里读绑定连接的隔离级别，应返回 "REPEATABLE_READ" */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String boundConnectionIsolation() throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        int level = connection.getTransactionIsolation();
        return switch (level) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN(" + level + ")";
        };
    }
}
