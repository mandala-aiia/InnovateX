package com.alec.InnovateX.spring.extension;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * BeanFactoryPostProcessor（BFPP）：所有 BeanDefinition 注册完毕之后、
 * 任何普通单例实例化之前回调——是修改"定义"（元数据）层面的最后窗口，
 * 比 BeanPostProcessor（修改"实例"）早了整整一个实例化阶段。
 *
 * 经典玩法：XML 时代用 BeanDefinitionVisitor 批量改写 <property> 里的占位符（如加密字段解密）；
 * 注解侧属性值写在 Java 代码里、不在 Definition 中，所以这里演示另一种等价能力——
 * 直接改定义元数据：把 onDemandService 的 lazy-init 从 false 翻成 true，
 * 效果等同给 @Bean 方法补 @Lazy，但"零侵入"（不碰业务代码与装配代码）。
 */
public class LazyFlipPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition definition = beanFactory.getBeanDefinition("onDemandService");
        System.out.println("[LazyFlipPostProcessor] 改写前 lazy-init=" + definition.isLazyInit());
        definition.setLazyInit(true);
        ExtensionTimeline.TIMELINE.add("bfpp:lazy-init已翻为true");
        System.out.println("[LazyFlipPostProcessor] 改写后 lazy-init=" + definition.isLazyInit()
                + "（此时尚无任何普通单例被实例化）");
    }
}
