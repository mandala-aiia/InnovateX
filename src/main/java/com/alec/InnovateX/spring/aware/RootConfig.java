package com.alec.InnovateX.spring.aware;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 根配置：@Import 导入 AwareConfig。
 * ImportAware 的回调只在"配置类被人 @Import"时发生——
 * 所以测试容器从 RootConfig 启动，AwareConfig 才能收到导入者（RootConfig）的元数据
 */
@Configuration
@Import(AwareConfig.class)
public class RootConfig {
}
