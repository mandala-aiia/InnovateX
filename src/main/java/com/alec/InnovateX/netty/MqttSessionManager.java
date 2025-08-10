package com.alec.InnovateX.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MqttSessionManager {
    public static final MqttSessionManager INSTANCE = new MqttSessionManager();

    private final Map<String, Channel> clientChannels = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> topicSubscribers = new ConcurrentHashMap<>();
    private final Map<Channel, String> channelToClient = new ConcurrentHashMap<>();

    private MqttSessionManager() {
    }

    public void registerClient(String clientId, Channel channel) {
        clientChannels.put(clientId, channel);
        channelToClient.put(channel, clientId);
    }

    public void subscribe(String clientId, String topic) {
        clientSubscriptions.computeIfAbsent(clientId, k -> new HashSet<>()).add(topic);
        topicSubscribers.computeIfAbsent(topic, k -> new HashSet<>()).add(clientId);
    }

    public void forwardToSubscribers(String topic, String payload) {
        Set<String> subscribers = topicSubscribers.getOrDefault(topic, Set.of());
        for (String clientId : subscribers) {
            Channel ch = clientChannels.get(clientId);
            if (ch != null && ch.isActive()) {
                ch.writeAndFlush(buildPublishMessage(topic, payload));
            }
        }
    }

    public void unregisterClient(Channel channel) {
        String clientId = channelToClient.remove(channel);
        if (clientId != null) {
            clientChannels.remove(clientId);
            Set<String> topics = clientSubscriptions.remove(clientId);
            if (topics != null) {
                for (String topic : topics) {
                    Set<String> subs = topicSubscribers.get(topic);
                    if (subs != null) {
                        subs.remove(clientId);
                        if (subs.isEmpty()) {
                            topicSubscribers.remove(topic);
                        }
                    }
                }
            }
        }
    }

    public String getClientId(Channel channel) {
        return channelToClient.get(channel);
    }

    private MqttPublishMessage buildPublishMessage(String topic, String payload) {
        MqttFixedHeader header = new MqttFixedHeader(
                MqttMessageType.PUBLISH,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                0);
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, 0);
        return new MqttPublishMessage(header, variableHeader, io.netty.buffer.Unpooled.copiedBuffer(payload, CharsetUtil.UTF_8));
    }
}
