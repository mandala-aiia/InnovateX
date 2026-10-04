package com.alec.InnovateX.spring.aware;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 根配置：唯一职责是 @Import 导入 ImportedFeatureConfig。
 * ImportAware 的回调只在"被别人导入"时发生——
 * 容器从这里启动，ImportedFeatureConfig 收到的导入者元数据就是本类。
 * 对比：@ComponentScan 是"批量按包导入"，@Import 是"精确按类导入"，两者都能触发 ImportAware。
 */
@Configuration
@Import(ImportedFeatureConfig.class)
public class BootstrapConfig {
}
