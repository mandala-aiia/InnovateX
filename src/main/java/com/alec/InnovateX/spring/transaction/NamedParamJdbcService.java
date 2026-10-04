package com.alec.InnovateX.spring.transaction;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * NamedParameterJdbcTemplate 专题：用 :name 命名占位符替代 ?。
 *
 * 对比 JdbcTemplate 的 ? 占位符：参数一多（或顺序调整）时 ? 极易错位且难读；
 * 命名参数与顺序无关，MapSqlParameterSource 还支持类型提示与多值展开（in (:ids)）。
 * 底层实现其实仍是 JdbcTemplate——NamedParameterJdbcTemplate 只是把 :name 解析成 ?。
 */
public class NamedParamJdbcService {

    private final NamedParameterJdbcTemplate namedTemplate;

    public NamedParamJdbcService(NamedParameterJdbcTemplate namedTemplate) {
        this.namedTemplate = namedTemplate;
    }

    /** 命名参数插入：SQL 里的 :orderNo/:amount 与参数源按键对位，与顺序无关 */
    public int insertOrder(String orderNo, int amount) {
        MapSqlParameterSource params = new MapSqlParameterSource("orderNo", orderNo)
                .addValue("amount", amount);
        int rows = namedTemplate.update(
                "insert into order_log (order_no, amount) values (:orderNo, :amount)", params);
        System.out.println("[NamedParamJdbcService] 命名参数插入 " + orderNo + "/" + amount + "，行数=" + rows);
        return rows;
    }

    /** 命名参数单值查询 */
    public Integer amountOfOrder(String orderNo) {
        return namedTemplate.queryForObject(
                "select amount from order_log where order_no = :orderNo",
                new MapSqlParameterSource("orderNo", orderNo),
                Integer.class);
    }

    /** 命名参数列表查询：条件值也是 :name，结果用 RowMapper 映射 */
    public List<String> orderNosAbove(int minAmount) {
        return namedTemplate.query(
                "select order_no from order_log where amount > :minAmount order by amount",
                new MapSqlParameterSource("minAmount", minAmount),
                (rs, rowNum) -> rs.getString("order_no"));
    }
}
