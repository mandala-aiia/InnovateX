package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 入站解码器：字节 -> ProtocolMessage。
 * 两个关键细节：
 * 1. 半包处理：可读字节不够一条完整消息时 markReaderIndex + resetReaderIndex 等待更多数据
 * 2. 魔数校验：非法报文直接断链，配合长度字段构成最基本的协议健壮性
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 6) {
            return; // 头部（魔数+类型+4字节长度）都不够，等下次
        }
        in.markReaderIndex();
        byte magic = in.readByte();
        if (magic != ProtocolMessage.MAGIC) {
            ctx.close();
            throw new CorruptedFrameException("魔数不匹配: 0x" + Integer.toHexString(magic & 0xFF));
        }
        byte type = in.readByte();
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex(); // 半包：回退读指针，等数据到齐重试
            return;
        }
        out.add(new ProtocolMessage(type, in.readCharSequence(length, StandardCharsets.UTF_8).toString()));
    }
}
