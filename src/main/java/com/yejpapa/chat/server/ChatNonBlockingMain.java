package com.yejpapa.chat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChatNonBlockingMain {

    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

    private void startEchoServer() {
        final int DEFAULT_PORT = 5555;

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            if (serverSocketChannel.isOpen() && selector.isOpen()) {
                serverSocketChannel.configureBlocking(false);

                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024);
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

                serverSocketChannel.bind(new InetSocketAddress(DEFAULT_PORT));

                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                System.out.println("Waiting for connections");

                while (true) {
                    selector.select();

                    Iterator keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();

                        keys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            acceptOP(key, selector);
                        } else if (key.isReadable()) {
                            readOP(key);
                        } else if (key.isWritable()) {
                            writeOP(key);
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> iterator = channelData.iterator();

        while (iterator.hasNext()) {
            byte[] it = iterator.next();
            iterator.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void readOP(SelectionKey key) {
        try{
            SocketChannel socketChannel = (SocketChannel) key.channel();

            buffer.clear();

            int numRead = -1;

            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("Connection Closed by : " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }

            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, StandardCharsets.UTF_8) + " from " + socketChannel.getRemoteAddress());

            doEchoJob(key, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();

        socketChannel.configureBlocking(false);

        System.out.println("Incoming connection from : " + socketChannel.getRemoteAddress());

        socketChannel.write(ByteBuffer.wrap("Hello!\n".getBytes(StandardCharsets.UTF_8)));

        keepDataTrack.put(socketChannel, new ArrayList<>());
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        ChatNonBlockingMain main = new ChatNonBlockingMain();
        main.startEchoServer();
    }
}
