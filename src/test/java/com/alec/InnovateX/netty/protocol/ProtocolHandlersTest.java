package com.alec.InnovateX.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * protocol 组 handler 测试（EmbeddedChannel，无需真实端口/文件路径）：
 * HTTP 文件服务器（目录列表/文件下载/404/302）、TCP 文件上传（整包+半包）、WebSocket 帧回显
 */
public class ProtocolHandlersTest {

    @TempDir
    Path tempDir;

    // ---------- HttpFileServerHandler ----------

    @Test
    public void httpDirectoryListing() throws Exception {
        Files.writeString(tempDir.resolve("a.txt"), "hello-file");
        Files.createDirectory(tempDir.resolve("sub"));
        EmbeddedChannel channel = newChannel();

        channel.writeInbound(get("/"));
        FullHttpResponse response = channel.readOutbound();
        assertEquals(HttpResponseStatus.OK, response.status());
        assertTrue(response.headers().get(HttpHeaderNames.CONTENT_TYPE).startsWith("text/html"));
        String html = response.content().toString(CharsetUtil.UTF_8);
        assertTrue(html.contains("a.txt") && html.contains("sub/"), html);
        System.out.println("HTTP 目录列表: 首页含 a.txt 与 sub/ ✓");
        response.release();
        channel.finishAndReleaseAll();
    }

    @Test
    public void httpFileDownload() throws Exception {
        String content = "hello-file-server";
        Files.writeString(tempDir.resolve("a.txt"), content);
        EmbeddedChannel channel = newChannel();

        channel.writeInbound(get("/a.txt"));
        // 响应三件套：HttpResponse（带 Content-Length）-> FileRegion（零拷贝）-> LastHttpContent
        io.netty.handler.codec.http.HttpResponse head = channel.readOutbound();
        assertEquals(HttpResponseStatus.OK, head.status());
        assertEquals(content.getBytes(StandardCharsets.UTF_8).length,
                Long.parseLong(head.headers().get(HttpHeaderNames.CONTENT_LENGTH)));
        Object body = channel.readOutbound();
        assertNotNull(body, "文件体（EmbeddedChannel 下为 FileRegion 或其拷贝）: " + body.getClass().getSimpleName());
        Object last = channel.readOutbound();
        assertTrue(last instanceof LastHttpContent, "应以 LastHttpContent 结尾: " + last);
        System.out.println("HTTP 文件下载: 200 + Content-Length=" + head.headers().get(HttpHeaderNames.CONTENT_LENGTH)
                + " + " + body.getClass().getSimpleName() + " + LastHttpContent ✓");
        channel.finishAndReleaseAll();
    }

    @Test
    public void httpNotFoundAndRedirect() throws Exception {
        EmbeddedChannel channel = newChannel();
        channel.writeInbound(get("/missing.txt"));
        FullHttpResponse notFound = channel.readOutbound();
        assertEquals(HttpResponseStatus.NOT_FOUND, notFound.status());
        notFound.release();
        System.out.println("HTTP 404: 不存在的文件 ✓");
        channel.finishAndReleaseAll();

        Files.createDirectory(tempDir.resolve("sub"));
        channel = newChannel();
        channel.writeInbound(get("/sub"));   // 目录但不以 / 结尾 -> 302 重定向
        FullHttpResponse redirect = channel.readOutbound();
        assertEquals(HttpResponseStatus.FOUND, redirect.status());
        assertEquals("/sub/", redirect.headers().get(HttpHeaderNames.LOCATION));
        redirect.release();
        System.out.println("HTTP 302: 目录重定向到 /sub/ ✓");
        channel.finishAndReleaseAll();
    }

    private EmbeddedChannel newChannel() {
        return new EmbeddedChannel(new HttpFileServerHandler("/", tempDir.toString()));
    }

    private static FullHttpRequest get(String uri) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
    }

    // ---------- FileUploadServerHandler ----------

    @Test
    public void fileUploadWholePacket() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new FileUploadServerHandler(tempDir.toString()));
        channel.writeInbound(encode("hello.txt", "文件上传整包测试"));
        assertEquals("hello.txt", channel.readInbound(), "接收完成信号应为文件名");
        assertFalse(channel.isOpen(), "传输完成后服务端应主动关闭连接");
        assertEquals("文件上传整包测试",
                Files.readString(tempDir.resolve("uploaded_hello.txt")));
        System.out.println("TCP 文件上传（整包）: uploaded_hello.txt 落盘且内容一致 ✓");
        channel.finishAndReleaseAll();
    }

    @Test
    public void fileUploadHalfPackets() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new FileUploadServerHandler(tempDir.toString()));
        byte[] frame = encodeBytes("split.bin", "半包".repeat(10));
        // 分三段喂入：恰好切在头部中间与内容中间——原实现会解析错乱，ByteToMessageDecoder 版正确等待
        channel.writeInbound(slice(frame, 0, 6));
        assertNull(channel.readInbound(), "头部不完整时不应产生完成信号");
        channel.writeInbound(slice(frame, 6, frame.length / 2));
        assertNull(channel.readInbound(), "内容未收完时不应产生完成信号");
        channel.writeInbound(slice(frame, frame.length / 2, frame.length));

        assertEquals("split.bin", channel.readInbound());
        assertEquals("半包".repeat(10), Files.readString(tempDir.resolve("uploaded_split.bin")));
        System.out.println("TCP 文件上传（三段半包）: 头部跨段/内容跨段均正确重组落盘 ✓");
        channel.finishAndReleaseAll();
    }

    private static ByteBuf slice(byte[] source, int from, int to) {
        return Unpooled.copiedBuffer(Arrays.copyOfRange(source, from, to));
    }

    /** 按协议编码：4B 文件名长度 + 文件名 + 8B 文件长度 + 内容 */
    private static ByteBuf encode(String name, String content) {
        return Unpooled.wrappedBuffer(encodeBytes(name, content));
    }

    private static byte[] encodeBytes(String name, String content) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.buffer(4 + nameBytes.length + 8 + body.length);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeLong(body.length);
        buf.writeBytes(body);
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        buf.release();
        return out;
    }

    // ---------- WebSocketFrameHandler ----------

    @Test
    public void webSocketFrameEcho() {
        EmbeddedChannel channel = new EmbeddedChannel(new WebSocketFrameHandler());
        channel.writeInbound(new TextWebSocketFrame("hello-ws"));
        TextWebSocketFrame response = channel.readOutbound();
        assertEquals("Echo: hello-ws", response.text());
        response.release();
        System.out.println("WebSocket 帧回显: hello-ws -> Echo: hello-ws ✓");
        channel.finishAndReleaseAll();
    }
}
