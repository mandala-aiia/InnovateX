package com.alec.InnovateX.spring.javaconfig;

/**
 * record 作为 Bean（JDK 21）：不可变载体，组件即属性——
 * 自动获得 equals/hashCode/toString，Spring 6+ 原生支持 record 参与 DI
 */
public record ServerEndpoint(String host, int port) {

    public String url() {
        return host + ":" + port;
    }
}
