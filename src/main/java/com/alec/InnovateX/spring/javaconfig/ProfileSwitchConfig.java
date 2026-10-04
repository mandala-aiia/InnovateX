package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @Profile 是 @Conditional 的官方特化（内部由 ProfileCondition 实现）：
 * 按激活的 profile 决定 Bean 是否注册，"激活了谁"由 Environment 说了算。
 * 既可标在配置类上，也可标在 @Bean 方法上（同一接口的多实现借它做环境切换）
 */
@Configuration
public class ProfileSwitchConfig {

    @Bean
    @Profile("dev")
    public DeployService devDeployService() {
        return new DevDeployService();
    }

    @Bean
    @Profile("prod")
    public DeployService prodDeployService() {
        return new ProdDeployService();
    }
}
