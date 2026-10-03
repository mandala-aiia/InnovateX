package com.alec.InnovateX.netty.baseline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * baseline 组 handler 测试：Echo 的 FixedLengthFrameDecoder 定长拆包、UDP 回显
 */
public class BaselineHandlersTest {

    @Test
    public void fixedLengthFrameEcho() {
        // 与 EchoServer 的 pipeline 一致：定长 20 字节拆帧 -> StringDecoder -> EchoServerHandler
        List<Object> frames = new ArrayList<>();
        EmbeddedChannel channel = new EmbeddedChannel(
                new FixedLengthFrameDecoder(20),
                new StringDecoder(CharsetUtil.UTF_8),
                // 统计拆出的帧（EchoServerHandler 只打印，这里补一个记录器）
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        frames.add(msg);
                        ctx.fireChannelRead(msg);
                    }
                },
                new EchoServerHandler());

        // 45 字节 = 2 个完整帧 + 10 字节凑不满一帧（等后续数据）
        channel.writeInbound(Unpooled.copiedBuffer("A".repeat(20) + "B".repeat(20) + "C".repeat(10), CharsetUtil.UTF_8));
        assertEquals(2, frames.size(), "45 字节按 20 定长应拆出 2 帧: " + frames);
        assertEquals("A".repeat(20), frames.get(0));
        assertEquals("B".repeat(20), frames.get(1));

        // 再补 10 字节：第三帧凑齐
        channel.writeInbound(Unpooled.copiedBuffer("D".repeat(10), CharsetUtil.UTF_8));
        assertEquals(3, frames.size());
        assertEquals("C".repeat(10) + "D".repeat(10), frames.get(2));
        System.out.println("定长拆包: 45+10 字节 -> " + frames.size() + " 帧（20/20/20），凑不满的半帧正确等待");
        channel.finishAndReleaseAll();
    }

    @Test
    public void udpEcho() {
        EmbeddedChannel channel = new EmbeddedChannel(new UdpServerHandler());
        InetSocketAddress sender = new InetSocketAddress("localhost", 7777);
        InetSocketAddress recipient = new InetSocketAddress("localhost", 8080);

        channel.writeInbound(new DatagramPacket(
                Unpooled.copiedBuffer("ping-udp", CharsetUtil.UTF_8), recipient, sender));

        DatagramPacket response = channel.readOutbound();
        assertEquals("Echo: ping-udp", response.content().toString(CharsetUtil.UTF_8));
        assertEquals(sender, response.recipient(), "回显目标应是发送方地址");
        response.release();
        System.out.println("UDP 回显: ping-udp -> Echo: ping-udp（回送到发送方 " + sender + "）");
        channel.finishAndReleaseAll();
    }
}
