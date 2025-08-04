package com.alec.InnovateX;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String request = msg.text();
        System.out.println("Server received: " + request);
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Echo: " + request));
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("Client connected: " + ctx.channel().id());
    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected: " + ctx.channel().id());
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
