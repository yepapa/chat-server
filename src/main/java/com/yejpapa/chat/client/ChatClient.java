package com.yejpapa.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Random;

public class ChatClient {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(ChatClient.class);

        final int DEFAULT_PORT = 5555;
        final String IP = "127.0.0.1";

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer helloBuffer = ByteBuffer.wrap("Hello !".getBytes());
        ByteBuffer randomBuffer;
        CharBuffer charBuffer;
        Charset charset = Charset.defaultCharset();
        CharsetDecoder charsetDecoder = charset.newDecoder();

        try (SocketChannel socketChannel = SocketChannel.open()) {
            if (socketChannel.isOpen()) {
                socketChannel.configureBlocking(true);

                socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);

                socketChannel.connect(new InetSocketAddress(IP, DEFAULT_PORT));

                if (socketChannel.isConnected()) {

                    socketChannel.write(helloBuffer);

                    while (socketChannel.read(buffer) != -1) {
                        buffer.flip();

                        charBuffer = charsetDecoder.decode(buffer);
                        System.out.println(charBuffer.toString());

                        if (buffer.hasRemaining()) {
                            buffer.compact();
                        } else {
                            buffer.clear();
                        }

                        int r = new Random().nextInt(100);
                        if (r == 50) {
                            System.out.println("50 was generated! Close the socket channel");
                            break;
                        } else {
                            randomBuffer = ByteBuffer.wrap("Random number : ".concat(String.valueOf(r)).getBytes());
                            socketChannel.write(randomBuffer);
                        }
                    }
                }
            } else {
                System.out.println("The connection cannot be established!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
