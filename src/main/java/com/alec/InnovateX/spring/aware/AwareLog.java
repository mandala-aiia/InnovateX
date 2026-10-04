package com.alec.InnovateX.spring.aware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aware 回调记录器：按触发顺序记录，测试断言精确时序。 */
public final class AwareLog {

    private static final List<String> EVENTS = Collections.synchronizedList(new ArrayList<>());

    private AwareLog() {
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
