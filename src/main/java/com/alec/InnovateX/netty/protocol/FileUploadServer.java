package com.alec.InnovateX.netty.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class FileUploadServer {
    private final int port;
    // 上传文件的保存目录（文件名前加 "uploaded_"）
    private final String saveDir;

    public FileUploadServer(int port, String saveDir) {
        this.port = port;
        this.saveDir = saveDir;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<Channel>() {
                 @Override
                 protected void initChannel(Channel ch) throws Exception {
                     // 自定义协议的解码式 handler（自带半包处理），保存目录由构造器注入
                     ch.pipeline().addLast(new FileUploadServerHandler(saveDir));
                 }
             });
            ChannelFuture f = b.bind(port).sync();
            System.out.println("FileUploadServer started on port " + port + "，保存目录: " + saveDir);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9000;
        // 默认保存到当前目录，可用 args[0] 指定
        String saveDir = args.length > 0 ? args[0] : ".";
        new FileUploadServer(port, saveDir).run();
    }
}
