package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 循环依赖的"另一端"：注入 AlarmCenter 的时机早于 AlarmCenter 初始化完成——
 * 因为存在循环依赖，它拿到的是三级缓存提前曝光的<b>代理</b>（而非原始对象），
 * 这正是"注入的就是代理且与最终代理同身份"断言的观察点。
 */
public class MonitorStation {

    @Autowired
    private AlarmCenter alarmCenter;

    public AlarmCenter getAlarmCenter() {
        return alarmCenter;
    }
}
