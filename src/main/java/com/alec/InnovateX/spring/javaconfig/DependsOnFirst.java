package com.alec.InnovateX.spring.javaconfig;

/** 记录初始化顺序：验证 @DependsOn 强制"先初始化 first 再初始化 second" */
public class DependsOnFirst {

    public static final java.util.List<String> INIT_ORDER = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    public DependsOnFirst() {
        INIT_ORDER.add("first");
        System.out.println("[DependsOnFirst] 初始化（第 " + INIT_ORDER.size() + " 个）");
    }
}
