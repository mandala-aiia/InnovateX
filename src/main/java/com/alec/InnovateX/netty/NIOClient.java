package com.alec.InnovateX.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class NIOClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(HOST, PORT));

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isConnectable()) {
                    // 完成连接
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    channel.register(selector, SelectionKey.OP_WRITE);
                    System.out.println("已连接到服务器");
                } else if (key.isWritable()) {
                    // 发送消息
                    System.out.print("请输入消息: ");
                    String input = scanner.nextLine();
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8));
                    channel.write(buffer);

                    // 注册读事件接收响应
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    // 读取响应
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = channel.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("服务器断开连接");
                        channel.close();
                        return;
                    }

                    buffer.flip();
                    String response = StandardCharsets.UTF_8.decode(buffer).toString();
                    System.out.println("收到服务端响应: " + response);
                    
                    // 继续注册写事件发送下一条消息
                    channel.register(selector, SelectionKey.OP_WRITE);
                }
            }
        }
    }
}