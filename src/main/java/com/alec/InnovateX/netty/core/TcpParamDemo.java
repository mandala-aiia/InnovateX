package com.alec.InnovateX.netty.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * TCP 参数与背压：ServerBootstrap 两级 option 的含义 + 写缓冲区高低水位。
 * - option(...)         配置的是"监听 Socket"：SO_BACKLOG 全连接队列长度（握手完成待 accept 的上限）
 * - childOption(...)    配置的是"每条连接"：SO_KEEPALIVE（TCP 层保活）、TCP_NODELAY（关 Nagle，
 *                       小包立即发送，低延迟场景必开）、SO_SNDBUF/RCVBUF 收发缓冲区
 * - WRITE_BUFFER_WATER_MARK：写缓冲积压超过高水位 isWritable()=false——
 *                       读写速度不匹配时的背压信号，应停止写入或丢弃，防止 OOM
 * telnet 127.0.0.1 8400 可测回显（限速 10KB/s，方便观察水位变化）
 */
public class TcpParamDemo {

    public static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(2);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new io.netty.channel.WriteBufferWaterMark(8 * 1024, 16 * 1024))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 限速 10KB/s：人为制造"读得快写得慢"，让积压容易超过高水位
                                    .addLast(new ChannelTrafficShapingHandler(10 * 1024))
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            if (ctx.channel().isWritable()) {
                                                ctx.writeAndFlush(msg); // 写操作接管释放责任
                                            } else {
                                                System.out.println("[背压] 写缓冲超过高水位，丢弃消息（isWritable=false）");
                                                ReferenceCountUtil.release(msg);
                                            }
                                        }
                                    });
                        }
                    });
            bootstrap.bind(8400).sync();
            System.out.println("TCP 参数演示服务器已启动: 8400（telnet 127.0.0.1 8400 测试回显；"
                    + "快速粘贴大文本可触发背压打印）");
            Thread.currentThread().join();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
