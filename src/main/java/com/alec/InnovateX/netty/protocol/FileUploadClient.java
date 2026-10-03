package com.alec.InnovateX.netty.protocol;

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
        // 上传目标文件：args[0] 可指定；默认 ~/Downloads/netty-upload-demo.txt（不存在则自动生成示例内容）
        java.nio.file.Path path = args.length > 0 ? java.nio.file.Paths.get(args[0])
                : java.nio.file.Paths.get(System.getProperty("user.home"), "Downloads", "netty-upload-demo.txt");
        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.writeString(path, "Netty 文件上传演示内容：InnovateX\n");
            System.out.println("已生成演示文件: " + path);
        }
        new FileUploadClient("localhost", 9000, path.toFile()).run();
    }
}
