package com.alec.InnovateX.spring.javaconfig;

/** 父子容器同名 Bean 的载体：两个容器各注册一个同名 messageBridge，看谁取到谁 */
public class SharedComponent {

    private final String origin;

    public SharedComponent(String origin) {
        this.origin = origin;
        System.out.println("[SharedComponent] 创建，来自" + origin);
    }

    public String origin() {
        return origin;
    }
}
