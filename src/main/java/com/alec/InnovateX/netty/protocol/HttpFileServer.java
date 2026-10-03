package com.alec.InnovateX.netty.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {
    private final int port;
    // URL 前缀；文件根目录由 HttpFileServerHandler 构造器注入（main 里默认 ~/Downloads，可用 args[0] 指定）
    private final String baseUrl;
    private final String root;

    public HttpFileServer(int port, String baseUrl) {
        this(port, baseUrl, System.getProperty("user.home") + java.io.File.separator + "Downloads");
    }

    public HttpFileServer(int port, String baseUrl, String root) {
        this.port = port;
        this.baseUrl = baseUrl;
        this.root = root;
    }

    public void run() throws Exception {
        // bossGroup 负责接收连接，workerGroup 负责处理 I/O 事件
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     .childHandler(new ChannelInitializer<Channel>() {
                         @Override
                         protected void initChannel(Channel ch) throws Exception {
                             ChannelPipeline pipeline = ch.pipeline();
                             // 解码 HTTP 请求
                             pipeline.addLast("decoder", new HttpRequestDecoder());
                             // 将多个 Http 消息聚合为一个 FullHttpRequest 或 FullHttpResponse
                             pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                             // 编码 HTTP 响应
                             pipeline.addLast("encoder", new HttpResponseEncoder());
                             // 支持分块写入
                             pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
                             // 业务处理器，处理文件读取及目录展示
                             pipeline.addLast("handler", new HttpFileServerHandler(baseUrl, root));
                         }
                     });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("HTTP 文件服务器已启动，访问地址：http://127.0.0.1:" + port + baseUrl);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
        // 监听端口，可根据需要修改
        int port = 8080;
        // URL前缀，这里设为"/"；文件根目录默认 ~/Downloads，可用第一个参数指定
        String root = args.length > 0 ? args[0]
                : System.getProperty("user.home") + java.io.File.separator + "Downloads";
        System.out.println("文件根目录: " + root);
        new HttpFileServer(port, "/", root).run();
    }
}
