package com.alec.InnovateX.netty.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.StandardCharsets;

/**
 * 业务线程隔离：pipeline.addLast(group, name, handler) 把耗时 handler 丢给独立线程池，
 * 避免业务阻塞卡死 IO 线程（EventLoop）——同一 Channel 的消息在业务组内仍保持串行。
 * 自测客户端连入后，观察两个 handler 打印的线程名：io 线程（nio-eventLoop）与 biz 线程各司其职
 */
public class BusinessThreadDemo {

    public static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(2);
        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(2, new DefaultThreadFactory("biz"));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 第一个 handler 跑在 IO 线程：只做轻量转发
                                    .addLast("ioHandler", new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            System.out.println("[IO 线程] " + Thread.currentThread().getName()
                                                    + " 收到数据，转交给业务 handler");
                                            ctx.fireChannelRead(msg);
                                        }
                                    })
                                    // 第二个 handler 指定业务线程池：addLast(EventExecutorGroup, name, handler)
                                    .addLast(businessGroup, "businessHandler", new SimpleChannelInboundHandler<ByteBuf>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                            System.out.println("[业务线程] " + Thread.currentThread().getName()
                                                    + " 处理=" + msg.toString(StandardCharsets.UTF_8)
                                                    + "（在这里 sleep/阻塞都不会卡住 IO 线程）");
                                        }
                                    });
                        }
                    });
            bootstrap.bind(8200).sync();
            System.out.println("业务线程隔离服务器已启动: 8200，自测连接中...");

            EventLoopGroup clientGroup = new NioEventLoopGroup(1);
            try {
                Bootstrap client = new Bootstrap()
                        .group(clientGroup)
                        .channel(NioSocketChannel.class);
                Channel channel = client.connect("127.0.0.1", 8200).sync().channel();
                channel.writeAndFlush(Unpooled.copiedBuffer("hello-业务隔离", StandardCharsets.UTF_8)).sync();
                channel.close().sync();
                Thread.sleep(500);
            } finally {
                clientGroup.shutdownGracefully();
            }
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            businessGroup.shutdownGracefully();
        }
    }
}
