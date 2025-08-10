package com.alec.InnovateX.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class UdpClient {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             // 开启广播（可选）
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new UdpClientHandler());

            // 绑定一个随机端口
            Channel ch = b.bind(0).sync().channel();

            // 发送消息到服务端 (此处服务端地址为 localhost:8080)
            String msg = "Hello UDP Server";
            DatagramPacket packet = new DatagramPacket(
                Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                new InetSocketAddress("127.0.0.1", 8080));
            ch.writeAndFlush(packet).sync();

            // 等待一段时间以便接收服务端的响应，然后关闭客户端
            ch.closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }
}
