package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/** 出站编码器：ProtocolMessage -> 字节（魔数+类型+长度+业务体），业务侧 write POJO 即可 */
public class MessageEncoder extends MessageToByteEncoder<ProtocolMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, ByteBuf out) {
        byte[] body = msg.getBody().getBytes(StandardCharsets.UTF_8);
        out.writeByte(ProtocolMessage.MAGIC);
        out.writeByte(msg.getType());
        out.writeInt(body.length);
        out.writeBytes(body);
    }
}
