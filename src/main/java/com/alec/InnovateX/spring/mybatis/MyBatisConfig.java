package com.alec.InnovateX.spring.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * MyBatis 纯 Spring 装配（与 Boot 无关），三个核心组件对应三个已演示过的 Spring 机制：
 * - @MapperScan：底层 MapperScannerRegistrar 实现 ImportBeanDefinitionRegistrar，
 *   用 ClassPathMapperScanner 扫描接口注册为 MapperFactoryBean——"接口没有实现类却能注入"全靠 FactoryBean 生成代理
 * - SqlSessionFactoryBean：本身就是一个 FactoryBean&lt;SqlSessionFactory&gt;（容器里暴露的是它 getObject() 的产物）
 * - SqlSessionTemplate：事务同步的核心——同一事务内所有 Mapper 调用共用一个 SqlSession
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.alec.InnovateX.spring.mybatis")
public class MyBatisConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("mybatis-demo/schema.sql")
                .build();
    }

    /** 注意返回类型是 SqlSessionFactoryBean（FactoryBean 本身），容器按类型暴露的是 SqlSessionFactory */
    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mybatis-demo/*.xml"));
        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true); // 下划线列名 -> 驼峰属性
        factory.setConfiguration(configuration);
        factory.setPlugins(new CountingSqlInterceptor()); // 注册 SQL 计数拦截器
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public UserService userService(UserMapper userMapper) {
        return new UserService(userMapper);
    }
}
