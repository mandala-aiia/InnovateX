package com.alec.InnovateX.spring.resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;

import java.util.Set;

/** 注册自定义 Converter 的容器级 ConversionService（bean 名称必须是 conversionService） */
@Configuration
public class ConversionConfig {

    @Bean
    public ConversionServiceFactoryBean conversionService() {
        ConversionServiceFactoryBean factory = new ConversionServiceFactoryBean();
        factory.setConverters(Set.of(new StringToOrderConverter()));
        return factory;
    }
}
