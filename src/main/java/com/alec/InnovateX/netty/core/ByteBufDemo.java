package com.alec.InnovateX.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;

import java.util.ArrayList;
import java.util.List;

/**
 * ByteBuf 三件套：堆/直接内存、视图共享语义、引用计数。
 * - slice()/duplicate()：与原 buf 共享底层存储（改一个另一个可见），只是独立的读写指针
 * - copy()：深拷贝，完全独立
 * - 引用计数：retain +1 / release -1，归零后不可再用，多次 release 抛 IllegalReferenceCountException；
 *   忘记 release 池化内存会泄漏，可用 -Dio.netty.leakDetection.level=PARANOID 排查
 */
public class ByteBufDemo {

    public static List<String> run() {
        List<String> notes = new ArrayList<>();

        // 1) 堆 vs 直接内存
        ByteBuf heap = Unpooled.buffer(8);
        heap.writeInt(0x12345678);
        notes.add("堆缓冲区 hasArray=" + heap.hasArray() + "（有底层数组，可 heap.array() 直取）");
        ByteBuf direct = Unpooled.directBuffer(8);
        notes.add("直接缓冲区 hasArray=" + direct.hasArray() + "（堆外内存，零拷贝 sendfile 场景更优）");

        // 2) slice/duplicate 共享 vs copy 独立
        ByteBuf slice = heap.slice(0, 4);
        heap.setInt(0, 99);
        notes.add("slice 共享底层存储=" + (slice.getInt(0) == 99) + "（改原 buf，slice 可见）");
        ByteBuf duplicate = heap.duplicate();
        duplicate.setInt(4, 777);
        notes.add("duplicate 共享底层存储=" + (heap.getInt(4) == 777) + "（只是独立了读写指针）");
        ByteBuf copy = heap.copy(0, 4);
        copy.setInt(0, 111);
        notes.add("copy 独立存储=" + (heap.getInt(0) == 99) + "（改 copy 不影响原 buf）");

        // 3) 引用计数
        ByteBuf buf = Unpooled.buffer(4);
        notes.add("新建 refCnt=" + buf.refCnt());
        buf.retain();
        notes.add("retain 后 refCnt=" + buf.refCnt() + "（跨 handler 传递所有权时使用）");
        buf.release();
        buf.release();
        notes.add("两次 release 后 refCnt=" + buf.refCnt() + "（已回收不可再用）");
        try {
            buf.release();
        } catch (IllegalReferenceCountException e) {
            notes.add("多余 release 抛 IllegalReferenceCountException ✓");
        }

        // 4) 池化分配（Netty 默认使用池化分配器）
        ByteBuf pooled = PooledByteBufAllocator.DEFAULT.directBuffer(16);
        notes.add("池化分配 refCnt=" + pooled.refCnt() + "，release 归还池=" + pooled.release());

        // 清理：slice/duplicate 与 heap 共享同一计数，释放一次即可
        heap.release();
        direct.release();
        copy.release();
        return notes;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
