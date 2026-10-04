package com.alec.InnovateX.spring.extension;

/**
 * BFPP 改写 BeanDefinition 的观测目标：
 * 装配侧声明为普通急切实例化（lazy-init=false），但 LazyFlipPostProcessor 会在
 * "定义已就绪、尚未实例化"的窗口期把它的 lazy-init 翻成 true——
 * 于是 refresh 结束它不存在，第一次 getBean 才构造（INSTANTIATED 即构造时刻的物证）。
 */
public class OnDemandService {

    /** 是否已被构造（构造器里置位，测试用它判断"何时才真正实例化"） */
    public static volatile boolean INSTANTIATED = false;

    public OnDemandService() {
        INSTANTIATED = true;
        ExtensionTimeline.TIMELINE.add("onDemandService:构造器");
        System.out.println("[OnDemandService] 构造器执行（只有 getBean 触发时才应看到这行）");
    }

    public String ping() {
        return "pong:按需服务";
    }
}
