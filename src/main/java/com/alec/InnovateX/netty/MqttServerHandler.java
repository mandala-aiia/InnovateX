package com.alec.InnovateX.netty;

import io.netty.channel.*;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;

import java.util.*;

public class MqttServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private final MqttSessionManager sessionManager = MqttSessionManager.INSTANCE;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage message) {
        switch (message.fixedHeader().messageType()) {
            case CONNECT -> handleConnect(ctx, (MqttConnectMessage) message);
            case SUBSCRIBE -> handleSubscribe(ctx, (MqttSubscribeMessage) message);
            case PUBLISH -> handlePublish(ctx, (MqttPublishMessage) message);
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
    }

    private void handlePublish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        String topic = msg.variableHeader().topicName();
        String payload = msg.payload().toString(CharsetUtil.UTF_8);
        System.out.println("📤 PUBLISH: topic=" + topic + ", payload=" + payload);

        // 转发给订阅了该主题的客户端
        sessionManager.forwardToSubscribers(topic, payload);
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
