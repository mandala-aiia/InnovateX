package com.alec.InnovateX.netty;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.channel.ChannelFutureListener;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        // 读取接收到的消息内容
        String msg = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println("服务端接收到消息: " + msg);

        // 构造回显消息
        String responseMsg = "Echo: " + msg;
        DatagramPacket response = new DatagramPacket(
            Unpooled.copiedBuffer(responseMsg, CharsetUtil.UTF_8),
            packet.sender());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         ctx.close();
    }
}
