package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 事件机制核心装配（注解式监听 + 接口式监听 + i18n），全部 @Bean 显式注册、不扫描，装配关系一目了然。
 *
 * 教学点：MessageSource 的 bean 名字必须是 messageSource——
 * AbstractApplicationContext.refresh() 里 initMessageSource() 按这个名字查找，
 * 找到后容器自身就具备 i18n 能力（ctx.getMessage(...) 直接可用），名字不对则静默退回空实现。
 * basename 直接引用 src/main/resources 已有的 message_*.properties（key：app.message）。
 */
@Configuration
public class CoreEventConfig {

    @Bean
    public EventRecorderListeners eventRecorderListeners() {
        return new EventRecorderListeners();
    }

    @Bean
    public LegacyInterfaceListener legacyInterfaceListener() {
        return new LegacyInterfaceListener();
    }

    /** @Bean 方法参数注入 ApplicationEventPublisher（容器注册的可解析依赖） */
    @Bean
    public InventoryService inventoryService(ApplicationEventPublisher publisher) {
        return new InventoryService(publisher);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("message");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
