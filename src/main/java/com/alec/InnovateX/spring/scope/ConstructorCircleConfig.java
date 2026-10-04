package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 构造器注入循环依赖装配：对比 {@link SetterCircleConfig}——同样的环，
 * 换成构造器注入后三级缓存彻底失效，refresh 直接失败。
 * 结论：构造器循环要么改 setter/字段注入，要么在注入点加 @Lazy（见 LazyCircleConfig）。
 */
@Configuration
public class ConstructorCircleConfig {

    @Bean
    public PayCore payCore(RiskCore riskCore) {
        return new PayCore(riskCore);
    }

    @Bean
    public RiskCore riskCore(PayCore payCore) {
        return new RiskCore(payCore);
    }
}
