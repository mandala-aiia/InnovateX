package com.alec.InnovateX.spring.aware;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ImportSelector：在"配置类解析期"（远早于任何 Bean 实例化）被调用，
 * 返回值是要额外导入容器的配置类全限定名数组——这是 Spring 向容器"编程式注册定义"的口子之一。
 *
 * 这里故意返回空数组：它存在的意义是搭出 ImportedFeatureConfig 头上的 @Import 结构，
 * 让"配置类不仅可以导入别的类，还能在导入结构里拿到导入者的元数据"这条链路完整可见：
 * selectImports 的入参 importingClassMetadata 就是导入它的那个类的注解元数据。
 */
public class FeatureImportSelector implements ImportSelector {

    /** 记录 selectImports 看到的导入者类名（应为 ImportedFeatureConfig）——与 ImportAware 的视角互补 */
    public static volatile String importerSeenBySelector;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        importerSeenBySelector = importingClassMetadata.getClassName();
        System.out.println("[FeatureImportSelector] 解析期被调用，导入者=" + importerSeenBySelector);
        return new String[0];
    }
}
