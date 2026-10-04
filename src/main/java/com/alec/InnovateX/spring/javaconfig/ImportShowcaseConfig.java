package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Import 三件套一次讲清：
 * 1) 普通类：无需任何注解直接注册，bean 名默认为全限定类名
 * 2) ImportSelector：返回一批类名，容器在配置类解析阶段批量注册（Spring Boot 自动装配的地基）
 * 3) ImportBeanDefinitionRegistrar：直接操作 BeanDefinitionRegistry 手工注册（MyBatis @MapperScan 的原理）
 * 三者都发生在"所有 Bean 实例化之前"的配置类解析阶段
 */
@Configuration
@Import({PlainImportedBean.class, ThemeImportSelector.class, ThemeRegistrar.class})
public class ImportShowcaseConfig {
}
