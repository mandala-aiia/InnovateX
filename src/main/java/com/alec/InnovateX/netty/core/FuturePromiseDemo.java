package com.alec.InnovateX.netty.core;

import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Netty 异步模型 Future/Promise：
 * - Future："还没出结果的占位符"，只能被动等待/监听（JDK Future 还要轮询，Netty 的支持监听器回调）
 * - Promise：可写视图——setSuccess/setFailure 主动写结果（对应 Netty 的 DefaultPromise 实现）
 * - sync() vs await()：都阻塞等结果；区别是失败时 sync 直接把异常抛给调用方，await 只置状态不抛
 *   （在 EventLoop 线程里调用自己持有结果的 sync/await 会死锁——Netty 会直接抛 BlockingOperationException）
 */
public class FuturePromiseDemo {

    public static List<String> run() throws Exception {
        List<String> notes = new ArrayList<>();

        // 1) Promise + 监听器：异步回调而不是阻塞等待
        Promise<String> promise = new DefaultPromise<>(ImmediateEventExecutor.INSTANCE);
        List<String> callback = new ArrayList<>();
        promise.addListener(f -> callback.add("监听器收到: " + f.getNow()));
        notes.add("setSuccess 前监听器未触发=" + callback.isEmpty());
        promise.setSuccess("done");
        notes.add("setSuccess 后监听器触发=" + (callback.size() == 1 && callback.get(0).contains("done"))
                + "（" + callback.get(0) + "）");

        // 2) 失败的 Promise：sync 抛出原始异常，await 只返回失败状态
        Promise<Void> failed = new DefaultPromise<>(ImmediateEventExecutor.INSTANCE);
        failed.setFailure(new IllegalStateException("演示失败"));
        String thrown;
        try {
            failed.sync();
            thrown = "未抛异常";
        } catch (Throwable t) {
            thrown = t.getClass().getSimpleName();
        }
        notes.add("failed.sync() 抛出=" + thrown + "（原始异常直接上抛）");
        notes.add("failed.await() 静默返回 isSuccess=" + failed.await().isSuccess() + "（不抛异常）");

        // 3) ChannelFuture：Netty IO 操作的返回值（bind/connect/write 都是）
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelFuture closeFuture = channel.close();
        boolean done = closeFuture.await(1, TimeUnit.SECONDS);
        notes.add("close 返回的 ChannelFuture 异步完成=" + (done && closeFuture.isSuccess())
                + "（实战用 addListener 而不是 sync 阻塞）");
        return notes;
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
