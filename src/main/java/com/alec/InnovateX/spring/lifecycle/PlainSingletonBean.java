package com.alec.InnovateX.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/** 普通单例 Bean：用 @PostConstruct 标记"已完成初始化" */
@Component
public class PlainSingletonBean {

    private static volatile boolean initialized = false;

    @PostConstruct
    public void init() {
        initialized = true;
        System.out.println("[PlainSingletonBean] @PostConstruct 完成");
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
