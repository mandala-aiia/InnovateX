package com.alec.InnovateX.spring.aware;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/** 配合 ImportAware 演示的普通 ImportSelector：AwareConfig 通过 @Import 引入它 */
public class AwareImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[0];
    }
}
