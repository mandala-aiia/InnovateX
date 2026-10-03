package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Import 三种形式汇总演示：普通类 / ImportSelector / ImportBeanDefinitionRegistrar
 */
@Configuration
@Import({ImportedBean.class, JavaImportSelector.class, JavaRegistrar.class})
public class ImportAggregateConfig {
}
