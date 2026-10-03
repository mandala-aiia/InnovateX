package com.alec.InnovateX;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


/**
 * 主应用不加载 spring 包下按主题组织的学习 demo（各自独立建容器验证，注册进 Boot 上下文
 * 会互相冲突：如 FullModeConfig/LiteModeConfig 的 orderDataSource 同名 bean）；
 * spring 包根下的原始 XML demo 类无注解，不受此过滤器影响
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(
        basePackages = "com.alec.InnovateX",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.alec\\.InnovateX\\.spring\\.(annotation|javaconfig|scope|aop|transaction|event|aware|lifecycle|extension|xmladvanced|spel|resource|cache|async|mvc)\\..*"
        )
)
public class InnovateXApplication {
    public static void main(String[] args) {
        SpringApplication.run(InnovateXApplication.class, args);
    }
}
