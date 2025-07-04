package com.alec.InnovateX;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

public class MqttServer {
    public static void main(String[] args) {
        int port = 1883;
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("mqttDecoder", new MqttDecoder());
                            p.addLast("mqttEncoder", MqttEncoder.INSTANCE);
                            p.addLast("mqttHandler", new MqttServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("🚀 MQTT Server started on port " + port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            //
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
