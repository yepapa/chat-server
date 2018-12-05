package com.yejpapa.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ChatMain {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ChatMain.class);

        final int DEFAULT_PORT = 5555;
        final String IP = "127.0.0.1";

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            if (serverSocketChannel.isOpen()) {
                serverSocketChannel.configureBlocking(true);

                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

                serverSocketChannel.bind(new InetSocketAddress(IP, DEFAULT_PORT));

                System.out.println("Waiting for connections");

                while (true) {

                    logger.info("test");

                    try (SocketChannel socketChannel = serverSocketChannel.accept()) {
                        System.out.println("InComing connection from: " + socketChannel.getRemoteAddress());

                        while (socketChannel.read(buffer) != -1) {
                            buffer.flip();

                            socketChannel.write(buffer);

                            if (buffer.hasRemaining()) {
                                buffer.compact();
                            } else {
                                buffer.clear();
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
