package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Profile 是 @Conditional 的官方特化版（ProfileCondition 实现）：
 * 只有激活的 profile 对应的 Bean 才会注册，Environment 决定"激活了谁"。
 * @Profile 既可标在类上，也可标在 @Bean 方法上（同一个接口的多个实现切换环境）
 */
@Configuration
public class ProfileConfig {

    @Bean
    @Profile("dev")
    public ProfileService devProfileService() {
        return new DevProfileService();
    }

    @Bean
    @Profile("prod")
    public ProfileService prodProfileService() {
        return new ProdProfileService();
    }
}
