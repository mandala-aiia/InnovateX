package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ReplayingDecoder 版解码器：对比 MessageDecoder——
 * 读不够时底层会抛 Signal 自动"回退重试"，因此不用手写 readableBytes 检查和 mark/reset，
 * 代价是部分操作性能略差（每层 decode 调用都要建立快照），且不支持集中写等操作
 */
public class ReplayingProtocolDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte magic = in.readByte();   // 数据不足时自动中断，数据到齐后从头重试
        if (magic != ProtocolMessage.MAGIC) {
            ctx.close();
            throw new IllegalStateException("魔数不匹配");
        }
        byte type = in.readByte();
        int length = in.readInt();
        out.add(new ProtocolMessage(type, in.readCharSequence(length, StandardCharsets.UTF_8).toString()));
    }
}
