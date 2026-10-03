package com.alec.InnovateX.netty;

import com.alec.InnovateX.netty.codec.CustomCodecDemo;
import com.alec.InnovateX.netty.codec.DelimiterCodecDemo;
import com.alec.InnovateX.netty.codec.LengthFieldCodecDemo;
import com.alec.InnovateX.netty.codec.LineBasedCodecDemo;
import com.alec.InnovateX.netty.codec.MessageDecoder;
import com.alec.InnovateX.netty.codec.ProtocolMessage;
import com.alec.InnovateX.netty.codec.ReplayingProtocolDecoder;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 编解码家族验证（EmbeddedChannel，无需真实端口）：
 * 换行/分隔符/长度字段拆包、自定义协议编解码、粘包与半包
 */
public class CodecTest {

    @Test
    public void lineBasedSplit() {
        List<String> frames = LineBasedCodecDemo.split("hello\nnetty 换行拆包\n第三行\r\n");
        assertEquals(List.of("hello", "netty 换行拆包", "第三行"), frames);
    }

    @Test
    public void delimiterSplit() {
        assertEquals(List.of("abc", "def"), DelimiterCodecDemo.split("abc$_def$_", "$_"));
        // 分隔符被剥离
        assertTrue(DelimiterCodecDemo.split("abc$_", "$_").get(0).equals("abc"));
    }

    @Test
    public void lengthFieldRoundTrip() {
        String payload = "长度字段拆包";
        byte[] frame = LengthFieldCodecDemo.encode(payload);
        // 前 4 字节长度前缀 == 业务体长度
        assertEquals(payload.getBytes(StandardCharsets.UTF_8).length, ByteBuffer.wrap(frame).getInt());
        assertEquals(payload.getBytes(StandardCharsets.UTF_8).length + 4, frame.length);
        // 往返一致
        assertEquals(payload, LengthFieldCodecDemo.decode(frame));
    }

    @Test
    public void customCodecRoundTrip() {
        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.TYPE_BUSINESS, "自定义协议消息");
        byte[] encoded = CustomCodecDemo.encode(message);
        // 头部 6 字节（魔数+类型+4 长度）+ 业务体
        assertEquals(6 + "自定义协议消息".getBytes(StandardCharsets.UTF_8).length, encoded.length);
        // MessageDecoder 与 ReplayingDecoder 两个版本解出同样结果
        assertEquals(message, CustomCodecDemo.decode(encoded));
        assertEquals(message, CustomCodecDemo.decodeWith(new ReplayingProtocolDecoder(), encoded).get(0));
    }

    @Test
    public void stickyPacketAndHalfPacket() {
        // 粘包：两条报文连在一起一次喂入，正确拆成两条
        ProtocolMessage first = new ProtocolMessage(ProtocolMessage.TYPE_BUSINESS, "第一条");
        ProtocolMessage second = new ProtocolMessage(ProtocolMessage.TYPE_HEARTBEAT, "ping");
        byte[] sticky = concat(CustomCodecDemo.encode(first), CustomCodecDemo.encode(second));
        assertEquals(List.of(first, second), CustomCodecDemo.decodeWith(new MessageDecoder(), sticky));

        // 半包：只喂前 8 个字节解不出任何消息（markReaderIndex/resetReaderIndex 在等数据）
        byte[] full = CustomCodecDemo.encode(first);
        byte[] half = java.util.Arrays.copyOf(full, 8);
        assertTrue(CustomCodecDemo.decodeWith(new MessageDecoder(), half).isEmpty());
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
