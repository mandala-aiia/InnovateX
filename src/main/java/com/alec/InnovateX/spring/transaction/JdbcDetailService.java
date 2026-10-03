package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * spring-jdbc 细节四件套：
 * 1) batchUpdate：批量 DML，一次网络往返提交多组参数（性能远高于循环单条 update）
 * 2) RowMapper：每行映射一个对象（ORM 的雏形），query() 内部按行回调
 * 3) ResultSetExtractor：整个 ResultSet 交给你自己遍历（一行多次读取/聚合场景），只回调一次
 * 4) 异常转译：SQLException（受检）-> DataAccessException（非受检）体系，
 *    JdbcTemplate 每次操作都过 SQLExceptionTranslator——这就是 Spring 消灭持久层受检异常的机制
 */
public class JdbcDetailService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDetailService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 批量插入：金额按序号放大 100 倍，方便断言聚合结果 */
    public int batchInsert(List<String> orderNos) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < orderNos.size(); i++) {
            batchArgs.add(new Object[]{orderNos.get(i), (i + 1) * 100});
        }
        int[] affected = jdbcTemplate.batchUpdate("insert into order_log (order_no, amount) values (?, ?)", batchArgs);
        System.out.println("[JdbcDetailService] batchUpdate 一次性写入 " + affected.length + " 行");
        return affected.length;
    }

    /** RowMapper：逐行映射 */
    public List<String> queryWithRowMapper() {
        return jdbcTemplate.query("select order_no from order_log order by id",
                (rs, rowNum) -> rs.getString("order_no"));
    }

    /** ResultSetExtractor：整表聚合，只回调一次 */
    public int totalAmountWithExtractor() {
        return jdbcTemplate.query("select amount from order_log", rs -> {
            int total = 0;
            while (rs.next()) {
                total += rs.getInt(1);
            }
            return total;
        });
    }

    /** 异常转译：坏 SQL 的 SQLException 被翻译成 BadSqlGrammarException（DataAccessException 子类） */
    public String badSqlGrammar() {
        try {
            jdbcTemplate.queryForObject("select * from order_log_not_exist", Integer.class);
            return "没有抛异常？";
        } catch (org.springframework.dao.DataAccessException e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            System.out.println("[JdbcDetailService] DataAccessException=" + e.getClass().getSimpleName()
                    + "，根因=" + root.getClass().getSimpleName() + "（SQLException 被转译为非受检异常）");
            return e.getClass().getSimpleName() + " <- " + root.getClass().getSimpleName()
                    + (root instanceof SQLException ? "（受检异常被转译掉）" : "");
        }
    }
}
