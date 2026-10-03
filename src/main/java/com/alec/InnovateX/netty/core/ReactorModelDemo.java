package com.alec.InnovateX.netty.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

/**
 * Reactor 三种线程模型对照——通过"处理请求的线程名"一眼看出区别：
 * - 单线程 Reactor（8101）：一个 EventLoop 既 accept 又处理 IO，一处阻塞全服务卡死
 * - 多线程 Reactor（8102）：一组线程既 accept 又处理 IO（Netty 里 boss/worker 传同一个 group）
 * - 主从 Reactor（8103）：boss 专职 accept，worker 专职 IO——Netty 标准姿势（其余 demo 全是这种）
 * main 启动三个端口的服务，再自连接发一条消息，观察每个模型用哪个线程干活
 */
public class ReactorModelDemo {

    public static void main(String[] args) throws Exception {
        EventLoopGroup single = new NioEventLoopGroup(1);
        EventLoopGroup multi = new NioEventLoopGroup(4);
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(4);
        try {
            start("单线程 Reactor（一个线程干所有事）", 8101, single, single);
            start("多线程 Reactor（一组线程干所有事）", 8102, multi, multi);
            start("主从 Reactor（boss 接客，worker 干活）", 8103, boss, worker);
            selfTest(8101, 8102, 8103);
            Thread.sleep(500);
        } finally {
            single.shutdownGracefully();
            multi.shutdownGracefully();
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private static void start(String model, int port, EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                System.out.println("[" + model + "] 处理线程=" + Thread.currentThread().getName()
                                        + "，收到=" + msg.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                });
        bootstrap.bind(port).sync();
        System.out.println("[" + model + "] 端口 " + port + " 已启动");
    }

    /** 自测客户端：依次连接三个端口各发一条消息 */
    private static void selfTest(int... ports) throws Exception {
        EventLoopGroup clientGroup = new NioEventLoopGroup(1);
        try {
            for (int port : ports) {
                Bootstrap bootstrap = new Bootstrap()
                        .group(clientGroup)
                        .channel(NioSocketChannel.class);
                Channel channel = bootstrap.connect("127.0.0.1", port).sync().channel();
                channel.writeAndFlush(Unpooled.copiedBuffer("ping-" + port, StandardCharsets.UTF_8)).sync();
                channel.close().sync();
            }
            Thread.sleep(300);
        } finally {
            clientGroup.shutdownGracefully();
        }
    }
}
