package com.alec.InnovateX.spring.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SmartLifecycle / SmartInitializingSingleton 主题装配（全部显式 @Bean，不用 @ComponentScan）：
 * refresh 阶段的完整剧本依次是——
 *   全体非懒单例实例化（含各自 @PostConstruct）
 *   → afterSingletonsInstantiated（SmartInitializingSingleton，一次）
 *   → finishRefresh 里按 phase 升序 start SmartLifecycle
 * close 阶段：按 phase 降序 stop SmartLifecycle → 再逐个销毁 Bean。
 * 声明顺序即注册顺序：hook 最先注册；worker 通过构造器参数依赖 hook，
 * "hook 的 @PostConstruct 早于 worker 创建"由依赖方向双重保证。
 */
@Configuration
public class SmartLifecycleConfig {

    @Bean
    public AllSingletonsReadyHook allSingletonsReadyHook() {
        return new AllSingletonsReadyHook();
    }

    @Bean
    public PlainWorkerBean plainWorkerBean(AllSingletonsReadyHook allSingletonsReadyHook) {
        return new PlainWorkerBean(allSingletonsReadyHook);
    }

    @Bean
    public StorageLifecycle storageLifecycle() {
        return new StorageLifecycle();
    }

    @Bean
    public ComputeLifecycle computeLifecycle(StorageLifecycle storageLifecycle) {
        return new ComputeLifecycle(storageLifecycle);
    }
}
