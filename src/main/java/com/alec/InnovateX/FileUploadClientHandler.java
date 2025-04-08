package com.alec.InnovateX;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {
    private final File file;
    
    public FileUploadClientHandler(File file) {
        this.file = file;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 协议格式：
        // 1. 文件名长度（int, 4字节）
        // 2. 文件名（UTF-8 编码）
        // 3. 文件长度（long, 8字节）
        // 4. 文件内容
        byte[] fileNameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
        int fileNameLength = fileNameBytes.length;
        long fileLength = file.length();
        
        ByteBuf header = Unpooled.buffer(4 + fileNameLength + 8);
        header.writeInt(fileNameLength);
        header.writeBytes(fileNameBytes);
        header.writeLong(fileLength);
        
        // 发送 header
        ctx.write(header);
        
        // 分块发送文件内容
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int readBytes;
        while ((readBytes = fis.read(buffer)) != -1) {
            ByteBuf content = Unpooled.copiedBuffer(buffer, 0, readBytes);
            ctx.write(content);
        }
        fis.close();
        
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         ctx.close();
    }
}
