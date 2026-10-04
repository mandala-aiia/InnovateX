package com.alec.InnovateX.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

/**
 * @DependsOn + @Lazy：
 * - apiGateway 与 networkInfra 之间没有注入关系；声明顺序上 apiGateway 在前、本会先创建。
 *   @DependsOn("networkInfra") 强制容器"先建 networkInfra 再建 apiGateway"，销毁顺序自动反序
 * - @Lazy：启动阶段只注册 BeanDefinition，第一次 getBean（或被其他非懒 Bean 注入）才真正实例化
 */
@Configuration
public class StartupOrderConfig {

    @Bean
    @DependsOn("networkInfra")
    public ApiGatewayBean apiGateway() {
        return new ApiGatewayBean();
    }

    @Bean
    public NetworkInfraBean networkInfra() {
        return new NetworkInfraBean();
    }

    @Bean
    @Lazy
    public DeferredBean deferredBean() {
        return new DeferredBean();
    }
}
