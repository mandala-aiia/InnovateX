package com.alec.InnovateX.spring.aop;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 多切面共同命中的靶子：pump() 会被两个 @Order 不同的切面同时环绕 */
public class PipelineService {

    public static final List<String> FLOW = new CopyOnWriteArrayList<>();

    public String pump() {
        FLOW.add("pump-target");
        System.out.println("[PipelineService] pump：目标方法执行");
        return "pumped";
    }
}
