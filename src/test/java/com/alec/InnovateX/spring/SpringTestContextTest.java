package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.testctx.ContextCreationCounter;
import com.alec.InnovateX.spring.testctx.GreetingService;
import com.alec.InnovateX.spring.testctx.ModeReporter;
import com.alec.InnovateX.spring.testctx.TestContextConfig;
import com.alec.InnovateX.spring.transaction.TxConfig;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * spring-test 的 TestContext 框架：与手写 AnnotationConfigApplicationContext 相对——
 * 声明式上下文（@ExtendWith + @ContextConfiguration）、测试字段直接注入被测 Bean、
 * 上下文按配置键缓存复用、@ActiveProfiles 进缓存键、@DirtiesContext 触发重建、
 * @Transactional 测试方法自动回滚（H2 分文不动）。
 * 注意缓存/重建断言依赖方法顺序，故用 @Order 固定执行顺序
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("lab")
@ContextConfiguration(classes = {TestContextConfig.class, TxConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringTestContextTest {

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private ModeReporter modeReporter;

    @Autowired
    private ContextCreationCounter creationCounter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    public void declarativeInjectionAndGreeting() {
        // 没有一行 new AnnotationConfigApplicationContext：上下文由 SpringExtension 构建，字段直接注入
        assertNotNull(greetingService);
        assertEquals("hello InnovateX (from TestContext)", greetingService.greet("InnovateX"));
        System.out.println("声明式注入: " + greetingService.greet("InnovateX"));
    }

    @Test
    @Order(2)
    public void activeProfileSelectsBean() {
        // @ActiveProfiles("lab") 已进入缓存键：命中的是 @Profile("lab") 那个装配版本
        assertEquals("lab", modeReporter.mode());
        System.out.println("@ActiveProfiles(lab) 生效: modeReporter=" + modeReporter.mode());
    }

    @Test
    @Order(3)
    public void contextCachedAcrossTestMethods() {
        // 同一配置键的多个测试方法共用同一个 ApplicationContext——计数器仍为 1
        assertEquals(1, creationCounter.createdCount());
        System.out.println("上下文缓存: 前 3 个测试方法共用 1 个 ApplicationContext");
    }

    @Test
    @Order(4)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void dirtyContextMarkedAfterThisMethod() {
        // 本方法仍在旧上下文里跑（标记在方法结束后生效）
        assertEquals(1, creationCounter.createdCount());
        System.out.println("@DirtiesContext(AFTER_METHOD): 本方法结束后上下文将被丢弃");
    }

    @Test
    @Order(5)
    public void contextRebuiltAfterDirties() {
        // 上一个方法弄脏了上下文：这里拿到的是重建后的新实例（含新的 H2 内存库）
        assertEquals(2, creationCounter.createdCount());
        assertEquals("hello rebuild (from TestContext)", greetingService.greet("rebuild"));
        System.out.println("上下文已重建: 累计创建 " + creationCounter.createdCount() + " 次");
    }

    @Test
    @Order(6)
    @Transactional
    public void testTransactionRollsBackAutomatically() {
        // TestContext 事务支持：方法包进事务，结束自动回滚——测试数据零残留
        jdbcTemplate.update("update account set balance = balance - ? where name = ?", 100, "alice");
        Integer balance = jdbcTemplate.queryForObject(
                "select balance from account where name = 'alice'", Integer.class);
        assertEquals(900, balance);
        System.out.println("@Transactional 测试方法内: alice=" + balance + "（方法结束将自动回滚）");
    }

    @Test
    @Order(7)
    public void rollbackLeftDatabasePristine() {
        // 上一个方法虽改了余额，但事务已回滚——本方法看到的是初始数据
        Integer balance = jdbcTemplate.queryForObject(
                "select balance from account where name = 'alice'", Integer.class);
        assertEquals(1000, balance);
        System.out.println("测试事务已回滚: alice=" + balance + "，数据库分文未动");
    }
}
