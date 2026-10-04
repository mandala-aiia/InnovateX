package com.alec.InnovateX.spring.event;

/** 异步演示专用事件（与同步监听器隔离，专属事件类型让断言不受其他演示影响） */
public class ShipmentDispatchedEvent {

    private final String trackingNo;

    public ShipmentDispatchedEvent(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getTrackingNo() {
        return trackingNo;
    }
}
