package com.alec.InnovateX.spring.component;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ImportSelector：@Import 引入它时，selectImports 返回的全限定类名被批量注册为 bean 定义。
 * 类本身不需要任何注解，也不经过组件扫描 —— 这是框架/中间件批量注册配置的标准扩展点。
 */
public class LegacyImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {FaxNotifier.class.getName()};
    }
}
