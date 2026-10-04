package com.alec.InnovateX.spring.javaconfig;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Import 第三种形式——ImportBeanDefinitionRegistrar：
 * 拿到 BeanDefinitionRegistry 后完全手工注册 BeanDefinition，懒加载/作用域等元数据都可随意改写。
 * 比 ImportSelector 更底层：selector 只能"点名类"，registrar 可以"亲手造定义"
 */
public class ThemeRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        System.out.println("[ThemeRegistrar] 手工注册 beanDefinition: registrarCraftedBean");
        registry.registerBeanDefinition("registrarCraftedBean",
                BeanDefinitionBuilder.genericBeanDefinition(RegistrarCraftedBean.class)
                        .setLazyInit(false)
                        .getBeanDefinition());
    }
}
