package com.alec.InnovateX.spring.spel;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** SpEL 演示配置：扫描本包（SpelPropertiesBean 中 @Value 引用了 @spelPropertiesBean） */
@Configuration
@ComponentScan
public class SpelConfig {
}
