package com.alec.InnovateX.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pipeline 出入站传播顺序——Netty 最容易踩的坑：
 * - 入站事件（数据进来）：head → tail 方向，只经过入站 handler
 * - 出站事件（数据出去）：tail → head 方向（与添加顺序相反），只经过出站 handler
 * - ctx.writeAndFlush：从"当前节点"向 head 方向找第一个出站 handler——
 *   位于它后面的出站 handler 会被跳过！（channel.write 则永远从 tail 开始）
 *
 * 本例 pipeline 物理顺序：head → A(入) → C(出) → B(入) → D(出) → tail
 * 期望事件序列：入站-A → 入站-B → 出站-C（B 的 ctx.write，D 被跳过）→ 出站-D → 出站-C（channel 写）
 */
public class PipelineOrderDemo {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    static class InboundA extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            EVENTS.add("入站-A");
            ctx.fireChannelRead(msg);
        }
    }

    static class InboundB extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            EVENTS.add("入站-B");
            // 关键：ctx 写——只经过 B 之前的出站 handler（C），D 被跳过
            ctx.writeAndFlush(msg);
        }
    }

    static class OutboundC extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            EVENTS.add("出站-C");
            ctx.write(msg, promise);
        }
    }

    static class OutboundD extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            EVENTS.add("出站-D");
            ctx.write(msg, promise);
        }
    }

    public static List<String> run() {
        EVENTS.clear();
        EmbeddedChannel channel = new EmbeddedChannel(new InboundA(), new OutboundC(), new InboundB(), new OutboundD());
        // 1) 入站数据：head → A →(跳过出站 C)→ B
        channel.writeInbound(Unpooled.wrappedBuffer("入站数据".getBytes(StandardCharsets.UTF_8)));
        // B 里 ctx.writeAndFlush → 出站-C → head → 进入出站队列
        // 2) channel 写出站数据：tail → D → C → head
        channel.writeOutbound(Unpooled.wrappedBuffer("出站数据".getBytes(StandardCharsets.UTF_8)));
        // 消费掉两条出站消息，避免泄漏
        channel.finishAndReleaseAll();
        return List.copyOf(EVENTS);
    }

    public static void main(String[] args) {
        System.out.println("pipeline: head → A(入) → C(出) → B(入) → D(出) → tail\n");
        run().forEach(e -> System.out.println("事件: " + e));
        System.out.println("\n结论: 入站 head→tail 只走进站 handler；出站 tail→head 走出站 handler（顺序反转）；");
        System.out.println("      B 中 ctx.write 只向 head 方向找，D 被跳过——这是 ctx.write 与 channel.write 的本质区别");
    }
}
