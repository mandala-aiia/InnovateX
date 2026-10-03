package com.alec.InnovateX.netty.core;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Sharable 注解——handler 能否被多条 channel 复用的开关：
 * - 默认（无注解）：一个 handler 实例只能加入一条 pipeline（内部维护 per-channel 状态）
 * - @Sharable：单例复用，但要求 handler 绝对无"每连接"状态（可共享的线程安全状态如 AtomicInteger 可以）
 * Netty 的 StringDecoder/IdleStateHandler 等内置组件都是非共享的（每次 new），
 * 而 Netty 的很多协议编解码器是 @Sharable 的
 */
public class SharableHandlerDemo {

    /** 无每连接实例状态 + 线程安全计数：符合 @Sharable 的要求 */
    @ChannelHandler.Sharable
    static class SharedCounterHandler extends ChannelInboundHandlerAdapter {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            count.incrementAndGet();
            ctx.fireChannelRead(msg);
        }

        int count() {
            return count.get();
        }
    }

    public static List<String> run() {
        List<String> notes = new ArrayList<>();

        // 1) 同一个 @Sharable 实例加入两条 channel：合法复用
        SharedCounterHandler shared = new SharedCounterHandler();
        EmbeddedChannel channel1 = new EmbeddedChannel(shared);
        EmbeddedChannel channel2 = new EmbeddedChannel(shared);
        channel1.writeInbound(Unpooled.copiedBuffer("a", io.netty.util.CharsetUtil.UTF_8));
        channel2.writeInbound(Unpooled.copiedBuffer("b", io.netty.util.CharsetUtil.UTF_8));
        notes.add("同一 @Sharable 实例跨 channel 复用，累计处理=" + shared.count());

        // 2) 非 @Sharable 的实例加入第二条 channel：Netty 在 handlerAdded 时拒绝
        //    （注意要注册后再 addLast，复用检查发生在 handlerAdded 回调里）
        ChannelInboundHandlerAdapter stateful = new ChannelInboundHandlerAdapter();
        EmbeddedChannel owner = new EmbeddedChannel();
        owner.pipeline().addLast(stateful);
        try {
            EmbeddedChannel second = new EmbeddedChannel();
            second.pipeline().addLast(stateful);   // 同一实例再入一条 pipeline
            notes.add("非 @Sharable 复用：未抛异常？");
        } catch (RuntimeException e) {
            // 4.1.x 的实际行为：DefaultChannelPipeline.checkMultiplicity 抛 ChannelPipelineException
            notes.add("非 @Sharable 复用抛 " + e.getClass().getSimpleName() + " ✓（" + e.getMessage() + "）");
        }

        channel1.finishAndReleaseAll();
        channel2.finishAndReleaseAll();
        owner.finishAndReleaseAll();
        return notes;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
