package com.alec.InnovateX.spring.annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 注解驱动装配入口：
 * - @ComponentScan：默认扫描本配置类所在包，@Component/@Repository/@Service/@Controller 都会被注册
 * - @PropertySource：把 properties 文件加入 Environment 的 PropertySource 链，供 @Value 占位符解析
 */
@Configuration
@ComponentScan
@PropertySource("classpath:annotation/annotation-app.properties")
public class AnnotationScanConfig {
}
