package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @Conditional 条件装配：标在 @Bean 方法上按方法判断，标在配置类上则整个配置类生效与否。
 * Spring Boot 的 @ConditionalOnProperty/@ConditionalOnClass 全家桶都是 @Conditional 的组合封装
 */
@Configuration
public class FeatureToggleConfig {

    @Bean
    @Conditional(OnTogglePropertyCondition.class)
    public ToggleGuardedBean toggleGuardedBean() {
        System.out.println("[FeatureToggleConfig] 条件成立，注册 toggleGuardedBean");
        return new ToggleGuardedBean();
    }
}
