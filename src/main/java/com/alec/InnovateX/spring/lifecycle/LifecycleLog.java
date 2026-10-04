package com.alec.InnovateX.spring.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 生命周期事件记录器：各演示 bean 把回调按发生顺序写进来，测试断言整条时间线。 */
public final class LifecycleLog {

    private static final List<String> EVENTS = Collections.synchronizedList(new ArrayList<>());

    private LifecycleLog() {
    }

    public static void record(String event) {
        EVENTS.add(event);
    }

    public static List<String> events() {
        return List.copyOf(EVENTS);
    }

    public static void reset() {
        EVENTS.clear();
    }
}
