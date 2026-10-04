package com.alec.InnovateX.spring.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 作用域事件记录器：观察「何时创建、是否销毁」。 */
public final class ScopeLog {

    private static final List<String> EVENTS = Collections.synchronizedList(new ArrayList<>());

    private ScopeLog() {
    }

    public static void record(String event) {
        EVENTS.add(event);
    }

    public static List<String> events() {
        return List.copyOf(EVENTS);
    }

    public static long countOf(String prefix) {
        return EVENTS.stream().filter(e -> e.startsWith(prefix)).count();
    }

    public static void reset() {
        EVENTS.clear();
    }
}
