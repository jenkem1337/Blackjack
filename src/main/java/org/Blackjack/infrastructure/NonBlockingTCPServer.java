package org.Blackjack.infrastructure;

import org.Blackjack.application.BlackjackTableManagerActor;
import org.Blackjack.application.PlayerManagerActor;
import org.Blackjack.application.RoomId;
import org.Blackjack.application.command.CreateRoom;
import org.Blackjack.application.command.IsUserExist;
import org.Blackjack.application.command.SavePlayer;
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
import java.util.concurrent.CompletableFuture;
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
    private final ActorRef playerManagerRef = system.fork(new PlayerManagerActor());
    private final Queue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    public NonBlockingTCPServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            initServer();
            System.out.println("Server started on port " + port);

            while (true) {
                selector.select();
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
        } else if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            String message = new String(data).trim();
            if(message.equals("EXIT")) {
                pendingTasks.offer(() -> closeClient(key));
                selector.wakeup();
            }

            else if (message.startsWith("REGISTER_PLAYER")) {
                // <REGISTER_PLAYER:USERNAME>
                playerManagerRef.send(new AsyncCommand<>(new SavePlayer(message.split(":")[1])))
                        .whenComplete(((response, throwable) -> {
                            pendingTasks.offer(() -> {
                                ClientContext ctx = (ClientContext) key.attachment();

                                if (throwable != null) {
                                    ctx.writeQueue.add(ByteBuffer.wrap(("ERROR:"+throwable.getMessage()+"\n").getBytes()));
                                }
                                else if(response instanceof SuccessResponse<?> successResponse) {
                                        Player player = (Player)successResponse.message();
                                        String str = "USER_CREATED:" + " PLAYER_ID=" + player.id().id() + ":USERNAME=" +player.username()+"\n";
                                        ctx.writeQueue.add(ByteBuffer.wrap(str.getBytes()));
                                }
                                else if(response instanceof ApplicationErrorResponse errorResponse) {
                                    ctx.writeQueue.add(ByteBuffer.wrap(("ERROR_RESPONSE:"+errorResponse.message()+"\n").getBytes()));
                                }

                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

                            });
                            selector.wakeup();
                        }));
            }

            else if(message.startsWith("CREATE_ROOM")){
                // CREATE_ROOM:USERNAME
                playerManagerRef.send(new AsyncCommand<>(new IsUserExist(message.split(":")[1])))
                        .thenCompose(response -> {
                            if (response instanceof SuccessResponse<?> success) {
                                Player player = (Player) success.message();
                                return managerRef.send(
                                        new AsyncCommand<>(new CreateRoom(player))
                                );
                            }
                            else  {
                                if (response instanceof  ApplicationErrorResponse applicationErrorResponse) {
                                    pendingTasks.offer(() -> {
                                        ClientContext ctx = (ClientContext) key.attachment();
                                        ctx.writeQueue.add(
                                                ByteBuffer.wrap("USER NOT FOUND\n".getBytes())
                                        );
                                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                                    });
                                    selector.wakeup();

                                    return CompletableFuture.completedFuture(null);
                                }
                            }
                            return null;
                        }).thenAccept(response -> {
                            if (response == null) {
                                return;
                            }

                            pendingTasks.offer(() -> {
                                ClientContext ctx = (ClientContext) key.attachment();

                                if (response instanceof SuccessResponse<?> success) {
                                    RoomCreatedAndRegistered payload =
                                            (RoomCreatedAndRegistered) success.message();

                                    String msg =
                                            "ROOM CREATED : Room Id " + payload.uuid() +
                                                    " : Player Id " + payload.playerID().id() + "\n";

                                    ctx.writeQueue.add(ByteBuffer.wrap(msg.getBytes()));
                                } else {
                                    ctx.writeQueue.add(
                                            ByteBuffer.wrap("ERROR\n".getBytes())
                                    );
                                }

                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                            });

                            selector.wakeup();
                        });

            }

            else {
                ClientContext ctx = (ClientContext) key.attachment();
                ctx.writeQueue.add(ByteBuffer.wrap("Unkonwn route message\n".getBytes()));
                key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
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
                return;
            }
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
