package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DelimiterBasedFrameDecoder：以自定义分隔符拆包（如 "$_"）。
 * 对比 LineBased 的增强：分隔符可自定；skipSeparator 参数可让解码时剥离分隔符（这里用带 strip 的构造）
 */
public class DelimiterCodecDemo {

    /** 按自定义分隔符拆分（stripSeparators=true 剥离分隔符） */
    public static List<String> split(String input, String delimiter) {
        ByteBuf delim = Unpooled.wrappedBuffer(delimiter.getBytes(StandardCharsets.UTF_8));
        EmbeddedChannel channel = new EmbeddedChannel(new DelimiterBasedFrameDecoder(1024, true, delim));
        channel.writeInbound(Unpooled.copiedBuffer(input, StandardCharsets.UTF_8));
        List<String> frames = new ArrayList<>();
        ByteBuf frame;
        while ((frame = channel.readInbound()) != null) {
            frames.add(frame.toString(StandardCharsets.UTF_8));
            frame.release();
        }
        channel.finishAndReleaseAll();
        return frames;
    }

    public static void main(String[] args) {
        System.out.println("分隔符: $_，输入: \"abc$_def$_\"");
        split("abc$_def$_", "$_").forEach(f -> System.out.println("拆出的帧: [" + f + "]"));
    }
}
