package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 非事务读服务：所有测试断言的统一出口。
 *
 * 为什么单独抽一个 Bean：
 * 1. 它自己不带 @Transactional，每次读都走自动提交的新连接——读到的一定是"已提交"的最新状态，
 *    用它断言事务结果才可靠（绝不能在被测事务的同一个连接里自己读自己）。
 * 2. snapshot() 用 ResultSetExtractor 手工遍历整表，顺便复习一遍"整结果集只回调一次"的用法。
 */
public class BalanceQueryService {

    private final JdbcTemplate jdbcTemplate;

    public BalanceQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 查单个账户余额（已提交视图） */
    public int balance(String name) {
        return jdbcTemplate.queryForObject(
                "select balance from account where name = ?", Integer.class, name);
    }

    /** 四账户快照，形如 "alice=1000, bob=1000, carol=1000, dave=1000"，仅用于日志展示 */
    public String snapshot() {
        return jdbcTemplate.query("select name, balance from account order by id", rs -> {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(rs.getString("name")).append('=').append(rs.getInt("balance"));
            }
            return sb.toString();
        });
    }
}
