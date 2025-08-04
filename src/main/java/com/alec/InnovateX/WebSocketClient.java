package com.alec.InnovateX;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

import java.net.URI;

public class WebSocketClient {
    public static void main(String[] args) throws Exception {
        String uri = "ws://localhost:8080/ws";
        URI websocketURI = new URI(uri);

        EventLoopGroup group = new NioEventLoopGroup();
        WebSocketClientHandshaker handshaker =
                WebSocketClientHandshakerFactory.newHandshaker(
                        websocketURI, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
        WebSocketClientHandler handler = new WebSocketClientHandler(handshaker);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new WebSocketClientInitializer(handler));
            Channel ch = b.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
            handler.handshakeFuture().sync();
            ch.writeAndFlush(new TextWebSocketFrame("Hello from client!"));
            Thread.sleep(2000);
            ch.close();
        } finally {
            group.shutdownGracefully();
        }
    }
}
