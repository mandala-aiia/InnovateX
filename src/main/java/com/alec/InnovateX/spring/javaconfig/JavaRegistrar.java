package com.alec.InnovateX.spring.javaconfig;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Import 的第三种形式——ImportBeanDefinitionRegistrar：
 * 拿到 BeanDefinitionRegistry 后可以完全手动注册 BeanDefinition（可设置作用域、懒加载、属性等）。
 * MyBatis 的 @MapperScan 就是靠 MapperScannerRegistrar 实现的。
 */
public class JavaRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        System.out.println("[JavaRegistrar] 手动注册 beanDefinition: registrarBean");
        registry.registerBeanDefinition("registrarBean",
                BeanDefinitionBuilder.genericBeanDefinition(RegistrarImportedBean.class)
                        .setLazyInit(false)
                        .getBeanDefinition());
    }
}
