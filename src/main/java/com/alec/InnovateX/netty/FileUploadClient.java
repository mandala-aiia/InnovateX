package com.alec.InnovateX.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.File;

public class FileUploadClient {
    private final String host;
    private final int port;
    private final File file;
    
    public FileUploadClient(String host, int port, File file) {
        this.host = host;
        this.port = port;
        this.file = file;
    }
    
    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<Channel>() {
                 @Override
                 protected void initChannel(Channel ch) throws Exception {
                     ch.pipeline().addLast(new FileUploadClientHandler(file));
                 }
             });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
        // 请确保指定的文件存在
        File file = new File("/Users/mandala/Desktop/1234.txt");
        new FileUploadClient("localhost", 9000, file).run();
    }
}
