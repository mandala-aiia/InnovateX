package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.mybatis.CountingSqlInterceptor;
import com.alec.InnovateX.spring.mybatis.MyBatisConfig;
import com.alec.InnovateX.spring.mybatis.User;
import com.alec.InnovateX.spring.mybatis.UserMapper;
import com.alec.InnovateX.spring.mybatis.UserService;
import com.alec.InnovateX.spring.mybatis.UserXmlMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MyBatis 纯 Spring 集成：注解/XML 两种 Mapper、动态 SQL、
 * 一级/二级缓存（以拦截器统计实际执行的 SQL 数实证）、事务回滚
 */
public class MyBatisTest {

    @Test
    public void annotationCrud() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MyBatisConfig.class)) {
            UserMapper mapper = ctx.getBean(UserMapper.class);
            // 插入：useGeneratedKeys 回填自增 id
            User user = new User("alice", "alice@demo.io");
            assertEquals(1, mapper.insert(user));
            assertNotNull(user.getId());
            System.out.println("插入后自增 id 回填: " + user);

            // 查询：下划线列名 -> 驼峰属性映射
            User loaded = mapper.selectById(user.getId());
            assertEquals(user, loaded);
            assertEquals(1, mapper.updateEmail(user.getId(), "new@demo.io"));
            assertEquals("new@demo.io", mapper.selectById(user.getId()).getEmail());

            assertEquals(1, mapper.selectAll().size());
            assertEquals(1, mapper.deleteById(user.getId()));
            assertNull(mapper.selectById(user.getId()));
            System.out.println("注解 Mapper CRUD 全部通过");
        }
    }

    @Test
    public void dynamicSql() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MyBatisConfig.class)) {
            UserXmlMapper mapper = ctx.getBean(UserXmlMapper.class);
            // <foreach> 批量插入
            assertEquals(3, mapper.insertBatch(List.of(
                    new User("bob", "bob@corp.io"),
                    new User("bobby", "bobby@corp.io"),
                    new User("carol", "carol@home.io"))));

            // <where>+<if>：全空条件 -> 查全表
            assertEquals(3, mapper.selectByCondition(null, null).size());
            // 只按用户名模糊
            assertEquals(2, mapper.selectByCondition("b", null).size());
            // 只按邮箱模糊
            assertEquals(2, mapper.selectByCondition(null, "corp").size());
            // 组合条件（交集）
            assertEquals(1, mapper.selectByCondition("bobby", "corp").size());
            System.out.println("动态 SQL（where/if/foreach）全部通过: " + mapper.selectByCondition(null, null));
        }
    }

    @Test
    public void firstLevelCache() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MyBatisConfig.class)) {
            UserMapper mapper = ctx.getBean(UserMapper.class);
            UserService service = ctx.getBean(UserService.class);
            User user = new User("cache", "cache@demo.io");
            mapper.insert(user);

            // 同一事务：SqlSessionTemplate 复用 SqlSession -> 一级缓存命中，第二次不执行 SQL
            CountingSqlInterceptor.reset();
            service.twiceSameQueryInTx(user.getId());
            assertEquals(1, CountingSqlInterceptor.EXECUTED_SQL.size(),
                    "同一事务内两次相同查询应只执行 1 次 SQL: " + CountingSqlInterceptor.EXECUTED_SQL);
            System.out.println("一级缓存（事务内）: 2 次查询 -> " + CountingSqlInterceptor.EXECUTED_SQL.size() + " 条 SQL");

            // 无事务：每次 Mapper 调用新开 SqlSession -> 一级缓存失效
            CountingSqlInterceptor.reset();
            service.twiceSameQueryNoTx(user.getId());
            assertEquals(2, CountingSqlInterceptor.EXECUTED_SQL.size(),
                    "无事务时两次查询应执行 2 次 SQL: " + CountingSqlInterceptor.EXECUTED_SQL);
            System.out.println("一级缓存失效（无事务新开 SqlSession）: 2 次查询 -> "
                    + CountingSqlInterceptor.EXECUTED_SQL.size() + " 条 SQL");
        }
    }

    @Test
    public void secondLevelCache() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MyBatisConfig.class)) {
            UserXmlMapper xmlMapper = ctx.getBean(UserXmlMapper.class);
            xmlMapper.insertBatch(List.of(new User("cached", "c@demo.io")));
            Long id = xmlMapper.selectByCondition(null, null).get(0).getId();

            // 跨事务（跨 SqlSession）两次相同查询：<cache/> 二级缓存命中
            CountingSqlInterceptor.reset();
            User first = xmlMapper.selectById(id);
            User second = xmlMapper.selectById(id);
            assertEquals(first, second);
            assertEquals(1, CountingSqlInterceptor.EXECUTED_SQL.size(),
                    "二级缓存应让第二次查询不再执行 SQL: " + CountingSqlInterceptor.EXECUTED_SQL);
            System.out.println("二级缓存（跨 SqlSession）: 2 次查询 -> "
                    + CountingSqlInterceptor.EXECUTED_SQL.size() + " 条 SQL");
        }
    }

    @Test
    public void transactionalRollback() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MyBatisConfig.class)) {
            UserMapper mapper = ctx.getBean(UserMapper.class);
            UserService service = ctx.getBean(UserService.class);

            assertThrows(IllegalStateException.class, () -> service.insertThenRollback("ghost", "ghost@demo.io"));
            assertTrue(mapper.selectAll().isEmpty(), "回滚后不应有任何数据: " + mapper.selectAll());
            System.out.println("@Transactional 回滚: 插入未落库（查询结果 " + mapper.selectAll() + "）");
        }
    }
}
