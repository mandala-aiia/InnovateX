package com.alec.InnovateX.spring.aware;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ImportAware：@Configuration 类专属的 Aware。
 * 只有"被人 @Import 导入"的配置类才会收到 setImportMetadata 回调，拿到导入者的注解元数据——
 * 第三方框架（典型如 MyBatis 的 @MapperScan）正是借此读取用户注解里的属性，
 * 再向容器注册自己的 BeanDefinition，实现"注解驱动 + 编程式注册"的组合。
 *
 * 注意：把它当主配置类直接启动（无人导入）时回调不会发生，
 * 所以测试必须从 BootstrapConfig（根配置）启动，才能观察到回调。
 */
@Configuration
@Import(FeatureImportSelector.class)
public class ImportedFeatureConfig implements ImportAware {

    private AnnotationMetadata importMetadata;

    /** 回调传入的是"导入我的人"的元数据；配合 getter 供测试检查 */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
        System.out.println("[ImportedFeatureConfig] ImportAware 回调，导入者=" + importMetadata.getClassName());
    }

    public AnnotationMetadata getImportMetadata() {
        return importMetadata;
    }

    @Bean
    public AwareSinkBean awareSinkBean() {
        return new AwareSinkBean();
    }
}
