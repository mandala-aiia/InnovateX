package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Import 第二种形式——ImportSelector：
 * selectImports 返回"类的全限定名数组"，容器把数组里的类统统注册为 BeanDefinition。
 * 入参还能拿到导入方的注解元数据，可据此决定导谁（Spring Boot 的
 * AutoConfigurationImportSelector 就是靠它读 @EnableAutoConfiguration 的属性）
 */
public class ThemeImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        System.out.println("[ThemeImportSelector] 被配置类导入: " + importingClassMetadata.getClassName());
        return new String[]{SelectorPickedBean.class.getName()};
    }
}
