package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 集合注入：
 * - List<T>：所有实现按 @Order / bean 名排序注入
 * - Map<String,T>：key 是 bean 名、value 是实现，可按名直达
 */
public class NotifierHub {

    private final List<NotificationSender> orderedSenders;

    private final Map<String, NotificationSender> senderMap;

    @Autowired
    public NotifierHub(List<NotificationSender> orderedSenders, Map<String, NotificationSender> senderMap) {
        this.orderedSenders = orderedSenders;
        this.senderMap = senderMap;
    }

    public List<String> channels() {
        return orderedSenders.stream().map(NotificationSender::channel).toList();
    }

    public Set<String> beanNames() {
        return senderMap.keySet();
    }

    public String channelOf(String beanName) {
        return senderMap.get(beanName).channel();
    }
}
