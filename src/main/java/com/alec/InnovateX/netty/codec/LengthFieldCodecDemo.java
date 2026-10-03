package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.nio.charset.StandardCharsets;

/**
 * 长度字段拆包（实际项目几乎唯一的选择）：发送端用 LengthFieldPrepender 在消息前补 4 字节长度，
 * 接收端用 LengthFieldBasedFrameDecoder 按长度切帧。
 *
 * LengthFieldBasedFrameDecoder 五个关键参数：
 *   maxFrameLength      最大帧长（防恶意长帧，超出抛 TooLongFrameException）
 *   lengthFieldOffset   长度字段从第几字节开始（本例 0）
 *   lengthFieldLength   长度字段占几字节（本例 4，int）
 *   lengthAdjustment    长度值的补偿（长度字段后面还有头字节时用）
 *   initialBytesToStrip 解码后剥离头部几字节（本例 4：把长度字段剥掉，只留业务体）
 */
public class LengthFieldCodecDemo {

    /** 编码：payload -> [4字节长度][payload]（注意 prepender 会产出"长度头"+"业务体"两条出站消息，需合并） */
    public static byte[] encode(String payload) {
        EmbeddedChannel channel = new EmbeddedChannel(new LengthFieldPrepender(4));
        channel.writeOutbound(Unpooled.copiedBuffer(payload, StandardCharsets.UTF_8));
        ByteBuf merged = Unpooled.buffer();
        ByteBuf part;
        while ((part = channel.readOutbound()) != null) {
            merged.writeBytes(part);
            part.release();
        }
        byte[] bytes = new byte[merged.readableBytes()];
        merged.readBytes(bytes);
        merged.release();
        channel.finishAndReleaseAll();
        return bytes;
    }

    /** 解码：[4字节长度][payload] -> payload（支持一次喂入多帧，天然解决粘包） */
    public static String decode(byte[] frame) {
        EmbeddedChannel channel = new EmbeddedChannel(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
        channel.writeInbound(Unpooled.wrappedBuffer(frame));
        ByteBuf in = channel.readInbound();
        String payload = in.toString(StandardCharsets.UTF_8);
        in.release();
        channel.finishAndReleaseAll();
        return payload;
    }

    public static void main(String[] args) {
        byte[] frame = encode("长度字段拆包");
        System.out.println("编码后字节数: " + frame.length + "（4 字节长度头 + "
                + "长度字段拆包".getBytes(StandardCharsets.UTF_8).length + " 字节业务体）");
        System.out.println("解码还原: [" + decode(frame) + "]");
        // 粘包证明：两帧连在一起喂入也能正确拆开
        byte[] sticky = concat(encode("第一帧"), encode("第二帧"));
        EmbeddedChannel channel = new EmbeddedChannel(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
        channel.writeInbound(Unpooled.wrappedBuffer(sticky));
        ByteBuf f;
        while ((f = channel.readInbound()) != null) {
            System.out.println("粘包输入中拆出的帧: [" + f.toString(StandardCharsets.UTF_8) + "]");
            f.release();
        }
        channel.finishAndReleaseAll();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
