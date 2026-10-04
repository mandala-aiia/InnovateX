package com.alec.InnovateX.spring.extension;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 扩展点主题的共享时间线：BFPP、实例化前后、初始化前后的各类回调按发生顺序写入同一条线，
 * 测试据此精确断言"一个 Bean 从定义到成品，各类扩展点分别在哪个环节介入"。
 */
public final class ExtensionTimeline {

    /** onDemandService 从"定义被改写"到"初始化完成"的扩展点介入时间线 */
    public static final List<String> TIMELINE = new CopyOnWriteArrayList<>();

    private ExtensionTimeline() {
    }
}
