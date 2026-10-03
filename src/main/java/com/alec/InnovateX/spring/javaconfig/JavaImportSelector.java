package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Import 的第二种形式——ImportSelector：
 * selectImports 返回"类的全限定名数组"，容器在配置类解析阶段把这些类注册为 BeanDefinition。
 * 这是 Spring Boot 自动装配（AutoConfigurationImportSelector）的实现基础。
 */
public class JavaImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        System.out.println("[JavaImportSelector] 导入类元数据: " + importingClassMetadata.getClassName());
        return new String[]{SelectorImportedBean.class.getName()};
    }
}
