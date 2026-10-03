package com.alec.InnovateX.netty.protocol;

import io.netty.channel.*;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;

import java.util.*;

/**
 * MQTT Broker 协议处理器（升级版）：
 * - CONNECT/SUBSCRIBE/PUBLISH/DISCONNECT 基础流程
 * - QoS 1 PUBLISH：向发布方回 PUBACK（转发仍按 QoS 0 简化处理）
 * - retain=1 的 PUBLISH：消息存入 broker，之后每个匹配的新订阅立即收到保留消息
 * - PINGREQ 心跳：回 PINGRESP
 */
public class MqttServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private final MqttSessionManager sessionManager = MqttSessionManager.INSTANCE;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage message) {
        switch (message.fixedHeader().messageType()) {
            case CONNECT -> handleConnect(ctx, (MqttConnectMessage) message);
            case SUBSCRIBE -> handleSubscribe(ctx, (MqttSubscribeMessage) message);
            case PUBLISH -> handlePublish(ctx, (MqttPublishMessage) message);
            case PINGREQ -> handlePingReq(ctx);
            case DISCONNECT -> handleDisconnect(ctx);
            default -> System.out.println("❓ Unknown MQTT Message Type: " + message.fixedHeader().messageType());
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        System.out.println("📡 CONNECT: clientId=" + msg.payload().clientIdentifier());
        MqttFixedHeader connAckFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false,
                MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader connAckVarHeader =
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);
        MqttConnAckMessage connAck = new MqttConnAckMessage(connAckFixedHeader, connAckVarHeader);
        ctx.writeAndFlush(connAck);
        sessionManager.registerClient(msg.payload().clientIdentifier(), ctx.channel());
    }

    private void handleSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        String clientId = sessionManager.getClientId(ctx.channel());
        for (MqttTopicSubscription s : msg.payload().topicSubscriptions()) {
            sessionManager.subscribe(clientId, s.topicFilter());
            System.out.println("📥 SUBSCRIBE: " + clientId + " => " + s.topicFilter());
        }

        List<Integer> grantedQos = new ArrayList<>();
        for (int i = 0; i < msg.payload().topicSubscriptions().size(); i++) {
            grantedQos.add(0);
        }

        MqttSubAckMessage subAck = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                new MqttSubAckPayload(grantedQos)
        );
        ctx.writeAndFlush(subAck);

        // 订阅建立后立即下发匹配的保留消息（MQTT 规范行为）
        for (MqttTopicSubscription s : msg.payload().topicSubscriptions()) {
            sessionManager.sendRetainedTo(s.topicFilter(), ctx.channel());
        }
    }

    private void handlePublish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        String topic = msg.variableHeader().topicName();
        String payload = msg.payload().toString(CharsetUtil.UTF_8);
        System.out.println("📤 PUBLISH: topic=" + topic + ", payload=" + payload
                + ", qos=" + msg.fixedHeader().qosLevel() + ", retain=" + msg.fixedHeader().isRetain());

        // QoS 1：向发布方确认收到（PUBACK）——传输仍按 QoS 0 转发（简化）
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            MqttMessage pubAck = new MqttMessage(
                    new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()));
            ctx.writeAndFlush(pubAck);
        }

        // retain=1：broker 存为保留消息，供之后的新订阅补发
        if (msg.fixedHeader().isRetain()) {
            sessionManager.storeRetained(topic, payload);
        }

        // 转发给当前匹配的所有订阅者（通配符感知）
        sessionManager.forwardToSubscribers(topic, payload);
    }

    private void handlePingReq(ChannelHandlerContext ctx) {
        System.out.println("💓 PINGREQ -> PINGRESP: " + sessionManager.getClientId(ctx.channel()));
        MqttMessage pingResp = new MqttMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0));
        ctx.writeAndFlush(pingResp);
    }

    private void handleDisconnect(ChannelHandlerContext ctx) {
        sessionManager.unregisterClient(ctx.channel());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        sessionManager.unregisterClient(ctx.channel());
    }
}
