package com.alec.InnovateX.spring.lifecycle;

/**
 * 满配 Bean 的被注入依赖：本身无逻辑，只为让"属性填充"这一步可被观察——
 * 它被 @Autowired setter 注入进 FullChainBean 的那一刻，就是生命周期第 02 步的物证。
 */
public class ChainDependency {

    @Override
    public String toString() {
        return "ChainDependency(仅供观察注入时机)";
    }
}
