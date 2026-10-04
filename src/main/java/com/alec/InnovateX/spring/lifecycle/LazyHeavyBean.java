package com.alec.InnovateX.spring.lifecycle;

/** 懒加载 bean：构造时记录，用于观察「@Lazy 的 bean 直到首次 getBean 才创建」。 */
public class LazyHeavyBean {

    public LazyHeavyBean() {
        LifecycleLog.record("构造:lazyHeavy");
    }
}
