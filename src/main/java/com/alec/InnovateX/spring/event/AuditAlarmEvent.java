package com.alec.InnovateX.spring.event;

/** 异常传播演示专用事件：与其他演示的事件类型隔离，避免监听器互相干扰 */
public class AuditAlarmEvent {

    private final String reason;

    public AuditAlarmEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
