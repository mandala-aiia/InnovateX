package com.alec.InnovateX.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
    private static final int PORT = 8080;
    private static final String EXIT_CMD = "bye";

    public static void main(String[] args) throws IOException {
        // 创建ServerSocketChannel并配置为非阻塞模式
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(PORT));

        // 创建Selector并注册服务端通道
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动，监听端口：" + PORT);

        while (true) {
            selector.select(); // 阻塞等待就绪的事件
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    // 处理新连接
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = server.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接: " + clientChannel.getRemoteAddress());
                } else if (key.isReadable()) {
                    // 处理读事件
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = clientChannel.read(buffer);

                    if (bytesRead == -1) {
                        // 连接关闭
                        System.out.println("客户端断开: " + clientChannel.getRemoteAddress());
                        clientChannel.close();
                        continue;
                    }

                    buffer.flip();
                    String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                    System.out.println("收到客户端消息: " + message);

                    if (EXIT_CMD.equalsIgnoreCase(message)) {
                        clientChannel.close();
                        continue;
                    }

                    // 注册写事件准备响应
                    String response = "服务端响应: " + message;
                    clientChannel.register(selector, SelectionKey.OP_WRITE, 
                            ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
                } else if (key.isWritable()) {
                    // 处理写事件
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    
                    if (buffer.hasRemaining()) {
                        clientChannel.write(buffer);
                    }
                    
                    // 取消写事件监听，避免持续触发
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }
            }
        }
    }
}