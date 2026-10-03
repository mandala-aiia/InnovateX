package com.alec.InnovateX.netty.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * IdleStateHandler 心跳检测：读空闲/写空闲/读写全空闲三个维度（单位秒），
 * 到期后以 userEventTriggered 事件通知（不是 channelRead！）。
 * 实战套路：长连接服务（MQTT 的 PINGREQ、WebSocket 的 ping）在 readerIdle 超时时
 * 判定客户端假死并踢下线；客户端在 writerIdle 到期前主动发心跳保活。
 */
public class IdleStateDemo {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    /**
     * 等待一次"读空闲"事件：idleSeconds 秒没有任何读操作即触发。
     * 注意 EmbeddedChannel 的 EmbeddedEventLoop 没有真实线程驱动定时任务，
     * 需要循环调用 runScheduledTasks() 手动推进（这也是 EmbeddedChannel 测试定时逻辑的标准姿势）
     *
     * @return 触发的事件名列表；超时未触发则包含"超时未触发"
     */
    public static List<String> awaitReaderIdle(int idleSeconds, int waitMillis) throws InterruptedException {
        EVENTS.clear();
        EmbeddedChannel channel = new EmbeddedChannel(
                new IdleStateHandler(idleSeconds, 0, 0),
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                        if (evt instanceof IdleStateEvent event && event.state() == IdleState.READER_IDLE) {
                            EVENTS.add(event.state().name());
                        }
                    }
                });
        long deadline = System.currentTimeMillis() + waitMillis;
        while (EVENTS.isEmpty() && System.currentTimeMillis() < deadline) {
            channel.runPendingTasks();  // 推进 EmbeddedEventLoop 里到期的定时任务
            Thread.sleep(20);
        }
        if (EVENTS.isEmpty()) {
            EVENTS.add("超时未触发");
        }
        channel.finishAndReleaseAll();
        return List.copyOf(EVENTS);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("启动 2 秒读空闲检测（模拟：客户端连上后一言不发）...");
        awaitReaderIdle(2, 6000).forEach(e -> System.out.println("收到空闲事件: " + e + "（实战中此时应踢掉假死连接）"));
    }
}
