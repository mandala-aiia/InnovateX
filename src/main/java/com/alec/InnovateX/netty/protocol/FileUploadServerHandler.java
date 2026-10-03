package com.alec.InnovateX.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * TCP 文件上传接收端（自定义协议：4B 文件名长度 + 文件名 + 8B 文件长度 + 文件内容）。
 *
 * 重写说明：原实现继承 ChannelInboundHandlerAdapter 手工读取——头部跨 TCP 分段时
 * 已读字段无法回退（半包 bug：4 字节长度读到一半或名字读到一半，下一段数据会被当成头部重新解析）。
 * 改用 ByteToMessageDecoder：它自带累积缓冲 + markReaderIndex/resetReaderIndex 等待语义，
 * 与 codec 主题里 MessageDecoder 的处理方式一致。接收完成后 out.add(文件名) 作为完成信号，
 * 便于 EmbeddedChannel 测试断言；随后关闭连接
 */
public class FileUploadServerHandler extends ByteToMessageDecoder {

    private final Path saveDir;

    private String fileName;
    private long fileLength;
    private long receivedBytes;
    private OutputStream output;

    public FileUploadServerHandler() {
        this("."); // 默认保存到工作目录（与原行为一致）
    }

    public FileUploadServerHandler(String saveDir) {
        this.saveDir = Paths.get(saveDir);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 阶段一：读取头部（文件名长度 + 文件名 + 文件长度）
        if (fileName == null) {
            in.markReaderIndex();
            if (in.readableBytes() < 4) {
                return; // 连长度字段都不完整
            }
            int fileNameLength = in.readInt();
            if (in.readableBytes() < fileNameLength + 8) {
                in.resetReaderIndex(); // 名字或长度字段不完整，回退等待
                return;
            }
            byte[] fileNameBytes = new byte[fileNameLength];
            in.readBytes(fileNameBytes);
            fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
            fileLength = in.readLong();
            System.out.println("Receiving file: " + fileName + ", size: " + fileLength);
            Files.createDirectories(saveDir);
            output = Files.newOutputStream(saveDir.resolve("uploaded_" + fileName));
        }

        // 阶段二：读取文件内容（一次可能只有一部分，剩余的等下一次 decode）
        long remaining = fileLength - receivedBytes;
        int chunk = (int) Math.min(remaining, in.readableBytes());
        if (chunk > 0) {
            byte[] bytes = new byte[chunk];
            in.readBytes(bytes);
            output.write(bytes);
            receivedBytes += chunk;
        }

        // 接收完毕：关流、发完成信号、断开连接
        if (receivedBytes >= fileLength) {
            output.close();
            System.out.println("File " + fileName + " received completely.");
            out.add(fileName);
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (output != null) {
            try {
                output.close();
            } catch (Exception ignored) {
            }
        }
        ctx.close();
    }
}
