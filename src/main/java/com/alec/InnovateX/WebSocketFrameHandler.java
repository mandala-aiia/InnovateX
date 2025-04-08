package com.alec.InnovateX;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // 处理关闭帧
        if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().close();
            return;
        }
        // 处理 Ping 帧，返回 Pong 帧
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 处理文本帧
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            System.out.println("接收到文本消息：" + request);
            // 回显消息给客户端
            ctx.channel().writeAndFlush(new TextWebSocketFrame("Echo: " + request));
            return;
        }
        // 处理二进制帧（这里只是简单地回显数据）
        if (frame instanceof BinaryWebSocketFrame) {
            ByteBuf content = frame.content();
            ctx.channel().writeAndFlush(new BinaryWebSocketFrame(content.retain()));
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
