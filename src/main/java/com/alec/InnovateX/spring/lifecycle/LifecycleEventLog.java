package com.alec.InnovateX.spring.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SmartLifecycle / SmartInitializingSingleton 主题的共享事件日志。
 * 为什么单独建类：两个 phase 不同的 SmartLifecycle + 普通单例 + 全体就绪钩子都要往
 * 同一条时间线里写事件，把静态状态收拢到一个"日志簿"里，测试就能对"完整启停剧本"
 * 做精确断言，而不是各 Bean 各自为政再拼接。
 */
public final class LifecycleEventLog {

    /** 容器从 refresh 到 close 的关键生命周期事件（按发生顺序追加） */
    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    private LifecycleEventLog() {
    }
}
