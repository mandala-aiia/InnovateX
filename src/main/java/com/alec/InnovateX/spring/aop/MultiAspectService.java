package com.alec.InnovateX.spring.aop;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 多切面共同命中的目标：work() 会被两个 @Order 不同的切面同时环绕 */
public class MultiAspectService {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    public String work() {
        EVENTS.add("target-work");
        return "done";
    }
}
