package com.alec.InnovateX;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    private final WebSocketClientHandler handler;

    public WebSocketClientInitializer(WebSocketClientHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new HttpClientCodec())
                .addLast(new HttpObjectAggregator(8192))
                .addLast(handler);
    }
}
