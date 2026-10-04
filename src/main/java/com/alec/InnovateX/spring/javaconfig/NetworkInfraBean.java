package com.alec.InnovateX.spring.javaconfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 底层网络设施：构造时把自己的名字记入共享启动日志，供断言初始化顺序 */
public class NetworkInfraBean {

    /** 跨上下文共享的启动顺序演示状态（测试用例前统一 clear） */
    public static final List<String> STARTUP_LOG = new CopyOnWriteArrayList<>();

    public NetworkInfraBean() {
        STARTUP_LOG.add("networkInfra");
        System.out.println("[NetworkInfraBean] 初始化（第 " + STARTUP_LOG.size() + " 个）");
    }
}
