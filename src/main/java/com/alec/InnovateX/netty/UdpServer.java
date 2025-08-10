package com.alec.InnovateX.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdpServer {
    private final int port;

    public UdpServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             // 广播模式，可以根据需求决定是否开启
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new UdpServerHandler());

            Channel ch = b.bind(port).sync().channel();
            System.out.println("UDP 服务端已启动，监听端口: " + port);
            ch.closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new UdpServer(8080).run();
    }
}
