package org.Blackjack.infrastructure;

import org.Blackjack.application.BlackjackTableManagerActor;
import org.Blackjack.application.RoomId;
import org.Blackjack.application.command.CreateRoom;
import org.Blackjack.application.response.RoomCreatedAndRegistered;
import org.Blackjack.domain.Player;
import org.Blackjack.domain.PlayerID;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class NonBlockingTCPServer {
    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private Set<SocketChannel> connections = ConcurrentHashMap.newKeySet();
    private final ActorSystem system = new ActorSystem();
    private final ActorRef managerRef = system.fork(new BlackjackTableManagerActor());
    private final Queue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    public NonBlockingTCPServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            initServer();
            System.out.println("Server started on port " + port);

            while (true) {
                selector.select(); // blocking until events
                runPendingTasks();
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
        var selectionKey = client.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(new ClientContext(client));
        System.out.println("New client connected: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            pendingTasks.offer(() -> closeClient(key));
            selector.wakeup();
            return;
        } else if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            String message = new String(data).trim();
            if(message.equals("EXIT")) {
                pendingTasks.offer(() -> closeClient(key));
                selector.wakeup();
            }
            else if(message.startsWith("CREATE")){
                PlayerID playerID = new PlayerID();
                System.out.println("Player Id : " +playerID.id().toString());
                managerRef.send(new AsyncCommand<>(new CreateRoom(new Player(playerID))))
                        .whenComplete(((response, throwable) -> {
                            pendingTasks.offer(() -> {
                                ClientContext ctx = (ClientContext) key.attachment();
                                if (throwable != null) {
                                    ctx.writeQueue.add(ByteBuffer.wrap("ERROR\n".getBytes()));
                                } else {
                                    if(response instanceof SuccessResponse<?> successResponse) {
                                        RoomCreatedAndRegistered payload = (RoomCreatedAndRegistered)successResponse.message();
                                        String str = "ROOM CREATED : " + " Room Id " + payload.uuid() + " : Player Id " + payload.playerID().id().toString()+"\n";
                                        ctx.writeQueue.add(ByteBuffer.wrap(str.getBytes()));

                                    }
                                }

                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                            });

                            selector.wakeup();

                        }));
            }
//            else if(message.startsWith("/broadcast ")) {
//                broadcast((client.getRemoteAddress() + " : " + message.substring(11) + "\n").getBytes());
//            }
            System.out.println("Received: " + message);
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
//        SocketChannel client = (SocketChannel) key.channel();
//        ByteBuffer buffer = (ByteBuffer) key.attachment();
//
//        if (buffer == null) {
//            // yazacak bir şey yok → write flag’ini kapat
//            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
//            return;
//        }
//
//        int written = client.write(buffer);
//
//        if (written == -1) {
//            connections.remove(client);
//            client.close();
//            return;
//        }
//
//        if (buffer.hasRemaining()) {
//            // partial write → sonra tekrar denenmek üzere OP_WRITE açık kalır
//            return;
//        }
//
//        // Yazma tamamlandı
//        key.attach(null); // buffer'ı temizle
//        key.interestOps(SelectionKey.OP_READ); // tekrar read moduna dön

        ClientContext ctx = (ClientContext) key.attachment();
        SocketChannel client = ctx.channel;

        while (true) {
            ByteBuffer buffer = ctx.writeQueue.peek();

            if (buffer == null) {
                // yazacak hiçbir şey kalmadı
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
                // socket buffer dolu → selector tekrar çağıracak
                return;
            }

            // buffer tamamen yazıldı
            ctx.writeQueue.poll();
        }

    }
    private void closeClient(SelectionKey key) {
        try {
            connections.remove((SocketChannel) key.channel());
            key.cancel();
            key.channel().close();
        } catch (IOException ignored) {}
    }

    private void closeServer() {
        try {
            if (serverChannel != null) serverChannel.close();
            if (selector != null) selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void runPendingTasks() {
        Runnable task;
        while ((task = pendingTasks.poll()) != null) {
            task.run();
        }
    }


}
