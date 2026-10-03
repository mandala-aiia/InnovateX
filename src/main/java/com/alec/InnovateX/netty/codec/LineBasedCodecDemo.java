package com.alec.InnovateX.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * LineBasedFrameDecoder：以 \n 或 \r\n 为边界拆包——文本协议（如 Redis RESP、FTP）最简单的拆包方式。
 * 注意它只负责"按行切出 ByteBuf"，不剥离换行符本身（要剥离需再叠一个 DelimiterBasedFrameDecoder 或自行处理）
 */
public class LineBasedCodecDemo {

    /** 把任意输入按行拆分（演示 + 测试共用） */
    public static List<String> split(String input) {
        EmbeddedChannel channel = new EmbeddedChannel(new LineBasedFrameDecoder(1024));
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
        System.out.println("输入: \"hello\r\nnetty 换行拆包\\n第三行\\n\"");
        split("hello\r\nnetty 换行拆包\n第三行\n").forEach(f -> System.out.println("拆出的帧: [" + f + "]"));
    }
}
