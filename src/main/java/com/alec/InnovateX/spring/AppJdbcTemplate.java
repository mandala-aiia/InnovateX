package com.alec.InnovateX.spring;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.UUID;

@Data
public class AppJdbcTemplate {

    private JdbcTemplate jdbcTemplate;

    private PlatformTransactionManager transactionManager;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public String query() {
        return jdbcTemplate.queryForObject("select uuid from innovatex limit 1", String.class);
    }

    public String insert() {
        String uuid = UUID.randomUUID().toString();
        jdbcTemplate.update("insert into innovatex.innovatex (UUID)values (?)", uuid);
        return uuid;
    }

    public String update() {
        String uuid = UUID.randomUUID().toString();
        try {
            jdbcTemplate.update("update innovatex.innovatex set UUID =? where id = 1", uuid);
            throw new RuntimeException("oh 这是xml类型的回滚异常！！！！！！！！");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {

        }
        return uuid;
    }

    public String programmatic() {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
        String uuid = UUID.randomUUID().toString();
        try {
            jdbcTemplate.update("update innovatex.innovatex set UUID =? where id = 1", uuid);
            throw new RuntimeException("oh....这式编程式事务异常！！！！！！！！");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transactionManager.rollback(status);
        } finally {
            if (!status.isCompleted()) {
                transactionManager.commit(status);
            }
        }
        return uuid;
    }

    public String namedParameter() {
        String uuid = UUID.randomUUID().toString();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uuid", uuid);
        namedParameterJdbcTemplate.update("insert into innovatex.innovatex (UUID)values (:uuid)",params);
        return uuid;
    }
}
