package com.alec.InnovateX.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 引介演示装配。proxyTargetClass = true 强制 CGLIB，原因：
 * ContractPaperService 没有任何自己的接口，若走 JDK 代理，代理只能"代表被引介的 Sealable"，
 * 无法代表目标类型本身——getBean(ContractPaperService.class) 会直接失败；
 * CGLIB 子类代理则两头都满足：既是 ContractPaperService 的子类，又实现了混入的 Sealable。
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class IntroductionConfig {

    @Bean
    public ContractPaperService contractPaperService() {
        return new ContractPaperService();
    }

    @Bean
    public SealIntroductionAspect sealIntroductionAspect() {
        return new SealIntroductionAspect();
    }
}
