package com.alec.InnovateX.spring.mybatis;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MyBatis 插件（拦截器）演示：拦截 StatementHandler.prepare——
 * 只有真正到数据库执行 SQL 才会走到这里，因此 EXECUTED_SQL 是"实际执行数"的准确计数，
 * 用它来实证一级/二级缓存命中（缓存命中不会产生新的 prepare）
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class CountingSqlInterceptor implements Interceptor {

    public static final List<String> EXECUTED_SQL = new CopyOnWriteArrayList<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        String sql = handler.getBoundSql().getSql();
        EXECUTED_SQL.add(sql);
        System.out.println("[MyBatis拦截器] 实际执行 SQL: " + sql.replaceAll("\\s+", " "));
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    public static void reset() {
        EXECUTED_SQL.clear();
    }
}
