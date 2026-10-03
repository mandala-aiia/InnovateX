package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @Conditional 条件装配：@Conditional 标在 @Bean 方法上则按方法判断，标在类上则整个配置类生效与否
 * Spring Boot 的 @ConditionalOnClass/@ConditionalOnProperty 等一系列注解都是 @Conditional 的组合封装
 */
@Configuration
public class ConditionalConfig {

    @Bean
    @Conditional(OnSystemPropertyCondition.class)
    public ConditionalBean conditionalBean() {
        System.out.println("[ConditionalConfig] 条件满足，注册 conditionalBean");
        return new ConditionalBean();
    }
}
