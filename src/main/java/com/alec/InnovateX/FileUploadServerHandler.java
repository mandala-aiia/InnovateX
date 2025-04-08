package com.alec.InnovateX;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {

    private enum State {
        READ_HEADER, // 读取文件名和文件长度
        READ_FILE    // 读取文件内容
    }
    
    private State state = State.READ_HEADER;
    private String fileName;
    private long fileLength;
    private long receivedBytes = 0;
    private FileOutputStream fos;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {
            if (state == State.READ_HEADER) {
                // 头部包括：4字节 文件名长度、文件名、8字节 文件长度
                if (buf.readableBytes() < 4) {
                    return; // 等待足够的数据
                }
                int fileNameLength = buf.readInt();
                if (buf.readableBytes() < fileNameLength) {
                    return;
                }
                byte[] fileNameBytes = new byte[fileNameLength];
                buf.readBytes(fileNameBytes);
                fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                if (buf.readableBytes() < 8) {
                    return;
                }
                fileLength = buf.readLong();
                System.out.println("Receiving file: " + fileName + ", size: " + fileLength);
                // 保存到当前目录下，文件名前加 "uploaded_"
                fos = new FileOutputStream("uploaded_" + fileName);
                state = State.READ_FILE;
            }
            if (state == State.READ_FILE) {
                int readable = buf.readableBytes();
                byte[] bytes = new byte[readable];
                buf.readBytes(bytes);
                fos.write(bytes);
                receivedBytes += readable;
                if (receivedBytes >= fileLength) {
                    System.out.println("File " + fileName + " received completely.");
                    fos.close();
                    ctx.close(); // 传输完毕后关闭连接
                }
            }
        } finally {
            buf.release();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         ctx.close();
    }
}
