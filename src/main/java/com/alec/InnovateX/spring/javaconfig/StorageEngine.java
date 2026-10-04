package com.alec.InnovateX.spring.javaconfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 存储引擎 POJO：只做一件事——把每次"真实创建"记进静态日志（CopyOnWriteArrayList 线程安全）。
 * full/lite 模式的本质差异是"@Bean 方法体到底执行了几次"，没有这份日志就无从观察
 */
public class StorageEngine {

    /** 跨上下文共享的创建记录（测试用例前统一 clear） */
    public static final List<String> CREATION_LOG = new CopyOnWriteArrayList<>();

    private final String label;

    public StorageEngine(String label) {
        this.label = label;
        CREATION_LOG.add(label);
        System.out.println("[StorageEngine] 真实创建 " + label + " @" + Integer.toHexString(System.identityHashCode(this)));
    }

    public String getLabel() {
        return label;
    }
}
