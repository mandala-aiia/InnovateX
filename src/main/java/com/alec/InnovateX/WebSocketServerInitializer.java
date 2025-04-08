package com.alec.InnovateX;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 添加 HTTP 编解码器，用于处理 WebSocket 握手前的 HTTP 请求
        ch.pipeline().addLast(new HttpServerCodec());
        // 聚合成 FullHttpRequest
        ch.pipeline().addLast(new HttpObjectAggregator(65536));
        // 处理 WebSocket 握手、Ping/Pong、Close 帧，路径为 "/ws"
        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        // 业务处理器：处理各类 WebSocket 帧（文本、二进制等）
        ch.pipeline().addLast(new WebSocketFrameHandler());
    }
}
