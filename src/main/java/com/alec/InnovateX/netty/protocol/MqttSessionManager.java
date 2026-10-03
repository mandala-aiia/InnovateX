package com.alec.InnovateX.netty.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 会话管理：连接注册、主题订阅（含 +/# 通配符）、消息转发、RETAINED 保留消息。
 * 单例状态全部为 ConcurrentHashMap；测试用 reset() 清场隔离
 */
public class MqttSessionManager {
    public static final MqttSessionManager INSTANCE = new MqttSessionManager();

    private final Map<String, Channel> clientChannels = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> topicSubscribers = new ConcurrentHashMap<>();
    private final Map<Channel, String> channelToClient = new ConcurrentHashMap<>();
    /** RETAINED 消息：topic -> payload（订阅匹配时立即补发） */
    private final Map<String, String> retainedMessages = new ConcurrentHashMap<>();

    private MqttSessionManager() {
    }

    public void registerClient(String clientId, Channel channel) {
        clientChannels.put(clientId, channel);
        channelToClient.put(channel, clientId);
    }

    public void subscribe(String clientId, String topicFilter) {
        clientSubscriptions.computeIfAbsent(clientId, k -> new HashSet<>()).add(topicFilter);
        topicSubscribers.computeIfAbsent(topicFilter, k -> new HashSet<>()).add(clientId);
    }

    /**
     * 通配符匹配（MQTT 3.1.1 规则）：
     * '+' 匹配单层（news/+ 匹配 news/tech 不匹配 news/tech/a）；
     * '#' 只能是最后一段，匹配任意剩余层级（含父级：news/# 也匹配 news）
     */
    static boolean matchTopic(String filter, String topic) {
        String[] f = filter.split("/", -1);
        String[] t = topic.split("/", -1);
        for (int i = 0; i < f.length; i++) {
            if ("#".equals(f[i])) {
                return true;    // 剩余任意层级（含零层）
            }
            if (i >= t.length) {
                return false;  // 过滤器比主题长且没有通配符可补
            }
            if (!"+".equals(f[i]) && !f[i].equals(t[i])) {
                return false;
            }
        }
        return f.length == t.length;
    }

    /** 转发：遍历所有订阅过滤器做通配符匹配，命中的客户端各收一份 */
    public void forwardToSubscribers(String topic, String payload) {
        for (Map.Entry<String, Set<String>> entry : topicSubscribers.entrySet()) {
            if (!matchTopic(entry.getKey(), topic)) {
                continue;
            }
            for (String clientId : entry.getValue()) {
                Channel ch = clientChannels.get(clientId);
                if (ch != null && ch.isActive()) {
                    ch.writeAndFlush(buildPublishMessage(topic, payload));
                }
            }
        }
    }

    /** 存储保留消息（retain=1 时调用；空 payload 按 MQTT 规范应清除保留，此处从简直接覆盖/清除） */
    public void storeRetained(String topic, String payload) {
        if (payload == null || payload.isEmpty()) {
            retainedMessages.remove(topic);
        } else {
            retainedMessages.put(topic, payload);
        }
    }

    /** 订阅建立时把匹配的保留消息立即下发给该订阅者 */
    public void sendRetainedTo(String topicFilter, Channel channel) {
        for (Map.Entry<String, String> entry : retainedMessages.entrySet()) {
            if (matchTopic(topicFilter, entry.getKey()) && channel.isActive()) {
                channel.writeAndFlush(buildPublishMessage(entry.getKey(), entry.getValue()));
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

    /** 测试隔离：清空全部会话状态（含保留消息） */
    public void reset() {
        clientChannels.clear();
        clientSubscriptions.clear();
        topicSubscribers.clear();
        channelToClient.clear();
        retainedMessages.clear();
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
