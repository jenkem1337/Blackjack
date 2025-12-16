package org.Blackjack.infrastructure;

import org.Blackjack.application.RoomId;
import org.Blackjack.domain.PlayerID;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NonBlockingTCPServer {
    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private Set<SocketChannel> connections = ConcurrentHashMap.newKeySet();
    public NonBlockingTCPServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            initServer();
            System.out.println("Server started on port " + port);

            while (true) {
                selector.select(); // blocking until events
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isValid()) {
                        if (key.isAcceptable()) handleAccept(key);
                        if (key.isReadable()) handleRead(key);
                        if (key.isWritable()) handleWrite(key);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeServer();
        }
    }
    private void broadcast(byte[] data) {
        for (SocketChannel client : connections) {
            SelectionKey key = client.keyFor(selector);
            if (key == null || !key.isValid()) continue;

            ByteBuffer buffer = ByteBuffer.wrap(data);

            key.attach(buffer);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        }

        selector.wakeup();

    }
    private void initServer() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        connections.add(client);
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            System.out.println("Client disconnected: " + client.getRemoteAddress());
            client.close();
            return;
        } else if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            String message = new String(data).trim();
            if(message.equals("exit")) {
                client.close();
                return;
            }
            else if(message.startsWith("/broadcast ")) {
                broadcast((client.getRemoteAddress() + " : " + message.substring(11) + "\n").getBytes());
            }
            System.out.println("Received: " + message);
//            key.attach(ByteBuffer.wrap(String.format("Hello Client %s", client.getRemoteAddress()).getBytes()));
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            //            writeToClient(client, "Hello Client\n");
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        if (buffer == null) {
            // yazacak bir şey yok → write flag’ini kapat
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            return;
        }

        int written = client.write(buffer);

        if (written == -1) {
            connections.remove(client);
            client.close();
            return;
        }

        if (buffer.hasRemaining()) {
            // partial write → sonra tekrar denenmek üzere OP_WRITE açık kalır
            return;
        }

        // Yazma tamamlandı
        key.attach(null); // buffer'ı temizle
        key.interestOps(SelectionKey.OP_READ); // tekrar read moduna dön
    }

    private void closeServer() {
        try {
            if (serverChannel != null) serverChannel.close();
            if (selector != null) selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
