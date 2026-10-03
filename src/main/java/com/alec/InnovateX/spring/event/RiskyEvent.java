package com.alec.InnovateX.spring.event;

/** 专用于"异常传播"演示的事件：避免与其他演示的监听器互相干扰 */
public class RiskyEvent {

    private final String message;

    public RiskyEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
