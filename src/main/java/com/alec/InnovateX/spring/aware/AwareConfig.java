package com.alec.InnovateX.spring.aware;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ImportAware：@Configuration 类专属的 Aware——被 @Import 导入的配置类
 * 可以通过它拿到"导入我的那个类"的注解元数据（MyBatis 的 @MapperScan 就靠它读配置）
 */
@Configuration
@Import(AwareImportSelector.class)
public class AwareConfig implements ImportAware {

    private AnnotationMetadata importMetadata;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
        System.out.println("[AwareConfig] ImportAware 回调，导入者: " + importMetadata.getClassName());
    }

    public AnnotationMetadata getImportMetadata() {
        return importMetadata;
    }

    @Bean
    public AllAwareBean allAwareBean() {
        return new AllAwareBean();
    }
}
