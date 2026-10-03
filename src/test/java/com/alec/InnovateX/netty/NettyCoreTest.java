package com.alec.InnovateX.netty;

import com.alec.InnovateX.netty.core.ByteBufDemo;
import com.alec.InnovateX.netty.core.FuturePromiseDemo;
import com.alec.InnovateX.netty.core.IdleStateDemo;
import com.alec.InnovateX.netty.core.PipelineOrderDemo;
import com.alec.InnovateX.netty.core.SharableHandlerDemo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Netty 核心机制验证（EmbeddedChannel）：
 * pipeline 出入站传播顺序、ByteBuf 引用计数与共享语义、Future/Promise 异步模型、IdleState 心跳
 */
public class NettyCoreTest {

    @Test
    public void pipelineOrder() {
        // pipeline: head → A(入) → C(出) → B(入) → D(出) → tail
        // 入站走 A→B；B 的 ctx.write 只向 head 方向找（只经 C，D 被跳过）；channel 写走 D→C
        assertEquals(List.of("入站-A", "入站-B", "出站-C", "出站-D", "出站-C"), PipelineOrderDemo.run());
    }

    @Test
    public void byteBufSemantics() {
        List<String> notes = ByteBufDemo.run();
        assertTrue(notes.contains("堆缓冲区 hasArray=true（有底层数组，可 heap.array() 直取）"), String.valueOf(notes));
        assertTrue(notes.contains("直接缓冲区 hasArray=false（堆外内存，零拷贝 sendfile 场景更优）"));
        assertTrue(notes.contains("slice 共享底层存储=true（改原 buf，slice 可见）"));
        assertTrue(notes.contains("duplicate 共享底层存储=true（只是独立了读写指针）"));
        assertTrue(notes.contains("copy 独立存储=true（改 copy 不影响原 buf）"));
        assertTrue(notes.contains("新建 refCnt=1"));
        assertTrue(notes.contains("retain 后 refCnt=2（跨 handler 传递所有权时使用）"));
        assertTrue(notes.contains("两次 release 后 refCnt=0（已回收不可再用）"));
        assertTrue(notes.contains("多余 release 抛 IllegalReferenceCountException ✓"));
        assertTrue(notes.contains("池化分配 refCnt=1，release 归还池=true"));
    }

    @Test
    public void futureAndPromise() throws Exception {
        List<String> notes = FuturePromiseDemo.run();
        assertTrue(notes.contains("setSuccess 前监听器未触发=true"), String.valueOf(notes));
        assertTrue(notes.contains("setSuccess 后监听器触发=true（监听器收到: done）"));
        assertTrue(notes.contains("failed.sync() 抛出=IllegalStateException（原始异常直接上抛）"));
        assertTrue(notes.contains("failed.await() 静默返回 isSuccess=false（不抛异常）"));
        assertTrue(notes.contains("close 返回的 ChannelFuture 异步完成=true（实战用 addListener 而不是 sync 阻塞）"));
    }

    @Test
    public void idleStateHeartbeat() throws Exception {
        // 1 秒无读操作 → READER_IDLE 事件经 userEventTriggered 回调
        assertEquals(List.of("READER_IDLE"), IdleStateDemo.awaitReaderIdle(1, 4000));
    }

    @Test
    public void sharableHandler() {
        List<String> notes = SharableHandlerDemo.run();
        assertTrue(notes.stream().anyMatch(n -> n.equals("同一 @Sharable 实例跨 channel 复用，累计处理=2")),
                String.valueOf(notes));
        assertTrue(notes.stream().anyMatch(n -> n.startsWith("非 @Sharable 复用抛") && n.contains("✓")),
                String.valueOf(notes));
    }
}
