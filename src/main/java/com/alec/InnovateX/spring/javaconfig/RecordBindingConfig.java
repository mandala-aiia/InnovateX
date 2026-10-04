package com.alec.InnovateX.spring.javaconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * record 与 Java Config 的组合演示：
 * - @Bean 直接返回 record，组件即只读属性；
 * - record 做"消费者"：规范构造器是唯一构造器，无需 @Autowired 即隐式绑定；
 * - record 与 @Component 组合时，record 组件上还能直接标 @Value（这里用 @Bean 参数演示等价写法）
 */
@Configuration
@PropertySource("classpath:annotation/annotation-app.properties")
public class RecordBindingConfig {

    @Bean
    public ServerEndpoint serverEndpoint() {
        return new ServerEndpoint("innovatex.local", 8080);
    }

    @Bean
    public HealthProbe healthProbe(ServerEndpoint endpoint) {
        return new HealthProbe(endpoint);
    }

    @Bean
    public AppInfo appInfo(@Value("${annotation.app.name}") String appName,
                           @Value("${annotation.app.version}") String version) {
        return new AppInfo(appName, version);
    }
}
