package com.alec.InnovateX.spring.javaconfig;

/** 父子容器演示的共享服务：source 标识它来自哪个容器 */
public class SharedService {

    private final String source;

    public SharedService(String source) {
        this.source = source;
    }

    public String source() {
        return source;
    }
}
