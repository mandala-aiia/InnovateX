package com.alec.InnovateX.spring.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * spring-jdbc 细节四件套：
 *
 * 1) batchUpdate：一次往返提交多组参数，远快于循环单条 update；
 * 2) RowMapper：每行回调一次，一行映射一个对象——ORM 的雏形；
 * 3) ResultSetExtractor：整个 ResultSet 只回调一次，自己控制 next() 游标——
 *    适合跨行聚合、一行多次读取等 RowMapper 表达不了的场景；
 * 4) 异常转译：JdbcTemplate 每次操作都过 SQLExceptionTranslator，
 *    把受检的 SQLException 翻译成统一体系的非受检 DataAccessException
 *    （表不存在 -> BadSqlGrammarException）——这就是 Spring 消灭持久层受检异常的机制。
 */
public class JdbcDetailService {

    /**
     * 行对象：显式 final 字段 + 构造器 + getter（不用 lombok），供 RowMapper 演示。
     */
    public static final class OrderRow {

        private final String orderNo;

        private final int amount;

        public OrderRow(String orderNo, int amount) {
            this.orderNo = orderNo;
            this.amount = amount;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public int getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return orderNo + ":" + amount;
        }
    }

    private final JdbcTemplate jdbcTemplate;

    public JdbcDetailService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 1) batchUpdate：金额按序号放大 100 倍（100/200/300...），方便断言聚合结果 */
    public int insertOrderBatch(List<String> orderNos) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < orderNos.size(); i++) {
            batchArgs.add(new Object[]{orderNos.get(i), (i + 1) * 100});
        }
        int[] affected = jdbcTemplate.batchUpdate(
                "insert into order_log (order_no, amount) values (?, ?)", batchArgs);
        System.out.println("[JdbcDetailService] batchUpdate 一次写入 " + affected.length + " 行");
        return affected.length;
    }

    /** 2) RowMapper：query() 内部逐行回调 lambda，(rs, rowNum) -> 对象 */
    public List<OrderRow> listAllOrders() {
        return jdbcTemplate.query(
                "select order_no, amount from order_log order by id",
                (rs, rowNum) -> new OrderRow(rs.getString("order_no"), rs.getInt("amount")));
    }

    /** 3) ResultSetExtractor：整表求和，只有一次回调，游标自己推进 */
    public int sumAllAmounts() {
        return jdbcTemplate.query("select amount from order_log", rs -> {
            int total = 0;
            while (rs.next()) {
                total += rs.getInt(1);
            }
            return total;
        });
    }

    /** 4) 异常转译：查不存在的表，SQLException 被翻译成 BadSqlGrammarException（非受检） */
    public String translateBadSql() {
        try {
            jdbcTemplate.queryForObject(
                    "select balance from account_table_not_exist where name = ?", Integer.class, "alice");
            return "竟然没有抛异常";
        } catch (DataAccessException e) {
            // 注意：这里 catch 的是 RuntimeException 子类——调用方再也不必 throws SQLException
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            boolean causeIsSqlException = root instanceof SQLException;
            System.out.println("[JdbcDetailService] 捕获 " + e.getClass().getSimpleName()
                    + "，根因 " + root.getClass().getSimpleName()
                    + " 是受检 SQLException：" + causeIsSqlException);
            return e.getClass().getSimpleName() + " <- " + root.getClass().getSimpleName()
                    + "，根因是SQLException=" + causeIsSqlException;
        }
    }
}
