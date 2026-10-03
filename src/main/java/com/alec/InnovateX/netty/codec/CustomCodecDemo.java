package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义协议编解码演示：业务 handler 直接收发 ProtocolMessage POJO，
 * 字节级细节被 MessageEncoder（出站）/MessageDecoder（入站）封装——"协议栈分层"的最小样例
 */
public class CustomCodecDemo {

    /** POJO -> 字节 */
    public static byte[] encode(ProtocolMessage message) {
        EmbeddedChannel channel = new EmbeddedChannel(new MessageEncoder());
        channel.writeOutbound(message);
        ByteBuf out = channel.readOutbound();
        byte[] bytes = new byte[out.readableBytes()];
        out.readBytes(bytes);
        out.release();
        channel.finishAndReleaseAll();
        return bytes;
    }

    /** 字节 -> POJO（MessageDecoder 版） */
    public static ProtocolMessage decode(byte[] bytes) {
        return decodeWith(new MessageDecoder(), bytes).get(0);
    }

    /** 字节 -> POJO 列表（支持一次喂入多条粘在一起的报文） */
    public static List<ProtocolMessage> decodeWith(io.netty.handler.codec.ByteToMessageDecoder decoder, byte[] bytes) {
        EmbeddedChannel channel = new EmbeddedChannel(decoder);
        channel.writeInbound(Unpooled.wrappedBuffer(bytes));
        List<ProtocolMessage> messages = new ArrayList<>();
        Object msg;
        while ((msg = channel.readInbound()) != null) {
            messages.add((ProtocolMessage) msg);
        }
        channel.finishAndReleaseAll();
        return messages;
    }

    public static void main(String[] args) {
        ProtocolMessage request = new ProtocolMessage(ProtocolMessage.TYPE_BUSINESS, "自定义协议消息");
        byte[] encoded = encode(request);
        System.out.println("编码后字节数: " + encoded.length + "（6 字节头 + 业务体）");
        System.out.println("MessageDecoder 解码还原: " + decode(encoded));
        System.out.println("ReplayingDecoder 解码还原: " + decodeWith(new ReplayingProtocolDecoder(), encoded).get(0));

        // 粘包：两条报文连一起也能正确拆出（甚至中间截断半包也可重试）
        byte[] sticky = concat(encode(request), encode(new ProtocolMessage(ProtocolMessage.TYPE_HEARTBEAT, "ping")));
        decodeWith(new MessageDecoder(), sticky).forEach(m -> System.out.println("粘包拆出的消息: " + m));

        // 半包：只喂前 8 个字节——不解码；再补齐——成功
        EmbeddedChannel channel = new EmbeddedChannel(new MessageDecoder());
        byte[] full = encode(new ProtocolMessage(ProtocolMessage.TYPE_BUSINESS, "半包重试"));
        channel.writeInbound(Unpooled.copiedBuffer(java.util.Arrays.copyOf(full, 8), 0, 8));
        System.out.println("半包时读到的消息数: " + (channel.readInbound() == null ? 0 : 1));
        channel.writeInbound(Unpooled.copiedBuffer(full, 8, full.length - 8));
        System.out.println("补齐后解码: " + channel.readInbound());
        channel.finishAndReleaseAll();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
