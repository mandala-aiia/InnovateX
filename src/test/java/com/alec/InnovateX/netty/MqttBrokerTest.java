package com.alec.InnovateX.netty;

import com.alec.InnovateX.netty.protocol.MqttSessionManager;
import com.alec.InnovateX.netty.protocol.MqttServerHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 手写 MQTT Broker 的全流程自动化测试（EmbeddedChannel 双客户端，无需真实端口）：
 * CONNECT/CONNACK、通配符订阅（+/#）、QoS1 PUBACK、RETAINED 保留消息、PINGREQ 心跳
 */
public class MqttBrokerTest {

    private static final AtomicInteger CLIENT_SEQ = new AtomicInteger();

    @BeforeEach
    public void resetSessions() {
        MqttSessionManager.INSTANCE.reset();  // 单例会话管理器，测试间清场隔离
    }

    /** 建立一个已完成 CONNECT 握手的模拟客户端（只挂业务 handler，直接收发 MqttMessage 对象，编解码另见 codec 主题） */
    private EmbeddedChannel connectClient() {
        EmbeddedChannel channel = new EmbeddedChannel(new MqttServerHandler());
        String clientId = "client-" + CLIENT_SEQ.incrementAndGet();
        channel.writeInbound(connect(clientId));
        MqttConnAckMessage connAck = channel.readOutbound();
        assertNotNull(connAck, "CONNECT 应收到 CONNACK");
        assertEquals(MqttMessageType.CONNACK, connAck.fixedHeader().messageType());
        return channel;
    }

    @Test
    public void subscribeAndForward() {
        EmbeddedChannel subscriber = connectClient();
        subscriber.writeInbound(subscribe(1, "news/tech"));
        MqttSubAckMessage subAck = subscriber.readOutbound();
        assertEquals(MqttMessageType.SUBACK, subAck.fixedHeader().messageType());

        EmbeddedChannel publisher = connectClient();
        publisher.writeInbound(publish("news/tech", "hello", MqttQoS.AT_MOST_ONCE, false, 0));

        // 订阅者收到转发
        MqttPublishMessage forwarded = subscriber.readOutbound();
        assertNotNull(forwarded, "订阅者应收到转发的 PUBLISH");
        assertEquals("news/tech", forwarded.variableHeader().topicName());
        assertEquals("hello", forwarded.payload().toString(CharsetUtil.UTF_8));
        System.out.println("基础流程: CONNECT/SUBSCRIBE/PUBLISH/转发 ✓");
        subscriber.finishAndReleaseAll();
        publisher.finishAndReleaseAll();
    }

    @Test
    public void wildcardSubscribe() {
        EmbeddedChannel plusSub = connectClient();   // '+' 单层通配
        plusSub.writeInbound(subscribe(1, "news/+"));
        plusSub.readOutbound(); // SUBACK

        EmbeddedChannel hashSub = connectClient();   // '#' 多层通配
        hashSub.writeInbound(subscribe(2, "news/#"));
        hashSub.readOutbound(); // SUBACK

        EmbeddedChannel publisher = connectClient();
        // 单层主题：+ 与 # 都命中
        publisher.writeInbound(publish("news/sports", "m1", MqttQoS.AT_MOST_ONCE, false, 0));
        assertEquals("m1", payloadOf(plusSub.readOutbound()));
        assertEquals("m1", payloadOf(hashSub.readOutbound()));
        // 多层主题：只有 # 命中
        publisher.writeInbound(publish("news/sports/football", "m2", MqttQoS.AT_MOST_ONCE, false, 0));
        assertNull(plusSub.readOutbound(), "+ 不应匹配多层主题");
        assertEquals("m2", payloadOf(hashSub.readOutbound()));
        System.out.println("通配符订阅: news/+ 单层命中、news/# 多层命中 ✓");
        plusSub.finishAndReleaseAll();
        hashSub.finishAndReleaseAll();
        publisher.finishAndReleaseAll();
    }

    @Test
    public void qos1PubAck() {
        EmbeddedChannel publisher = connectClient();
        publisher.writeInbound(publish("any/topic", "qos1-body", MqttQoS.AT_LEAST_ONCE, false, 7));

        MqttMessage ack = publisher.readOutbound();
        assertInstanceOf(MqttMessageIdVariableHeader.class, ack.variableHeader(), "应收到 PUBACK");
        assertEquals(MqttMessageType.PUBACK, ack.fixedHeader().messageType());
        assertEquals(7, ((MqttMessageIdVariableHeader) ack.variableHeader()).messageId());
        System.out.println("QoS 1: PUBLISH -> PUBACK(messageId=7) ✓");
        publisher.finishAndReleaseAll();
    }

    @Test
    public void retainedMessage() {
        EmbeddedChannel publisher = connectClient();
        publisher.writeInbound(publish("cfg/color", "blue", MqttQoS.AT_MOST_ONCE, true, 0));

        // 新订阅者订阅该主题：SUBACK 之后立即收到保留消息
        EmbeddedChannel lateSubscriber = connectClient();
        lateSubscriber.writeInbound(subscribe(1, "cfg/color"));
        assertInstanceOf(MqttSubAckMessage.class, lateSubscriber.readOutbound());
        MqttPublishMessage retained = lateSubscriber.readOutbound();
        assertNotNull(retained, "新订阅者应立即收到 RETAINED 消息");
        assertEquals("cfg/color", retained.variableHeader().topicName());
        assertEquals("blue", retained.payload().toString(CharsetUtil.UTF_8));
        System.out.println("RETAINED: 保留消息随订阅立即下发 ✓");
        publisher.finishAndReleaseAll();
        lateSubscriber.finishAndReleaseAll();
    }

    @Test
    public void pingReqHeartbeat() {
        EmbeddedChannel client = connectClient();
        client.writeInbound(new MqttMessage(new MqttFixedHeader(
                MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0)));
        MqttMessage pingResp = client.readOutbound();
        assertEquals(MqttMessageType.PINGRESP, pingResp.fixedHeader().messageType());
        System.out.println("心跳: PINGREQ -> PINGRESP ✓");
        client.finishAndReleaseAll();
    }

    // ---- 报文构造辅助 ----

    private static MqttConnectMessage connect(String clientId) {
        return new MqttConnectMessage(
                new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnectVariableHeader("MQTT", 4, false, false, false, 0, false, true, 60),
                new MqttConnectPayload(clientId, null, "", null, ""));
    }

    private static MqttSubscribeMessage subscribe(int messageId, String filter) {
        return new MqttSubscribeMessage(
                new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                new MqttSubscribePayload(List.of(new MqttTopicSubscription(filter, MqttQoS.AT_MOST_ONCE))));
    }

    private static MqttPublishMessage publish(String topic, String payload, MqttQoS qos, boolean retain, int messageId) {
        return new MqttPublishMessage(
                new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, retain, 0),
                new MqttPublishVariableHeader(topic, messageId),
                Unpooled.copiedBuffer(payload, CharsetUtil.UTF_8));
    }

    private static String payloadOf(MqttPublishMessage message) {
        return message == null ? null : message.payload().toString(CharsetUtil.UTF_8);
    }
}
