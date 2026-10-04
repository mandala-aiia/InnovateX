package com.alec.InnovateX.spring.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;

import java.util.Set;

/**
 * 注册容器级 ConversionService。
 * <p>
 * 关键约定：<b>Bean 名字必须是 conversionService</b>（类型是 ConversionService），
 * 容器启动时按名字查找并把它设为 BeanFactory 的全局类型转换服务；
 * 名字换成别的它就只是个普通 Bean，@Value/依赖注入的类型转换不会用它。
 * <p>
 * ConversionServiceFactoryBean 是个 FactoryBean：容器里名为 conversionService 的
 * 实际对象是它 getObject() 产出的 DefaultConversionService（含全部内置转换器）+ 我们的定制项。
 */
@Configuration
public class ConversionConfig {

    @Bean
    public static ConversionServiceFactoryBean conversionService() {
        ConversionServiceFactoryBean factory = new ConversionServiceFactoryBean();
        // 只需注册"增量"：内置的 String->Number/集合/布尔等转换器由工厂默认补齐
        factory.setConverters(Set.of(new StringToServerNodeConverter()));
        return factory;
    }

    /** 演示"容器自动用 ConversionService"：字符串字面量 → ServerNode 参数 */
    @Bean
    public ClusterRegistry clusterRegistry(@Value("redis.innovatex:6379") ServerNode redisNode) {
        return new ClusterRegistry(redisNode);
    }
}
