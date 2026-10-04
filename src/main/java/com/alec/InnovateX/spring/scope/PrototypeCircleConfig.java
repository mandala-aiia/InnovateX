package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * prototype 循环依赖装配：Bean 定义合法，容器也能顺利 refresh
 * （prototype 默认懒创建，refresh 时不实例化）——第一次 getBean 时才爆炸：
 * UnsatisfiedDependencyException，根因是 BeanCurrentlyInCreationException。
 * <p>
 * 对比 singleton 循环：singleton 有"正在创建"的三级缓存可以救；
 * prototype 的"正在创建"标记只能用来检测并打断递归。
 */
@Configuration
public class PrototypeCircleConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JobShardA jobShardA(JobShardB partner) {
        JobShardA a = new JobShardA();
        a.setPartner(partner);
        return a;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JobShardB jobShardB(JobShardA partner) {
        JobShardB b = new JobShardB();
        b.setPartner(partner);
        return b;
    }
}
