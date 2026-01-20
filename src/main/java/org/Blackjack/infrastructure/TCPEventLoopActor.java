package org.Blackjack.infrastructure;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPEventLoopActor extends AbstractActor{
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private int port;
    private ActorRef acceptorActor;
    private SelectorExecutor selectorExecutor;
    private final Map<SelectionKey, ActorRef> connectionActors = new HashMap<>();
    private Transport transport;
    private ProtocolHandler protocolHandler;

    public TCPEventLoopActor(Transport transport, ProtocolHandler protocolHandler) {
        this.transport = transport;
        this.protocolHandler = protocolHandler;
    }
    private final Thread eventLoopThread = Thread.ofPlatform().unstarted(() -> {

        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            eventLoop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    private void eventLoop() throws IOException {

        System.out.println("Event loop thread started");
        while (true) {
            selector.select();
            selectorExecutor.executeTasks();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isValid()) {
                    if (key.isAcceptable())
                        acceptorActor.send(new AsyncCommand<>(new IOEvent.Acceptable(key)));
                    else {
                        var connectionActor = (ActorRef) key.attachment();
                        if (key.isReadable())
                            connectionActor.send(new AsyncCommand<>(new IOEvent.Readable()));
                        if (key.isWritable())
                            connectionActor.send(new AsyncCommand<>(new IOEvent.Writable()));
                    }
                }
            }
        }

    }


    @Override
    public Response onReceive(Command command) {
        return switch(command.message()) {
            case IOEvent.Start start ->  startServer(start);
            case IOEvent.PostAcceptation postAcceptation -> postAcceptation(postAcceptation);
            case IOEvent.InterestOps interestOps -> interestOps(interestOps);
            case IOEvent.Closed closed-> closeClient(closed);
            default -> throw new IllegalArgumentException();
        };

    }

    private Response startServer(IOEvent.Start start) {
        try {
            acceptorActor = context().fork(new AcceptorActor());
            selector = Selector.open();
            selectorExecutor = new SelectorExecutor(selector, new ConcurrentLinkedQueue<>());
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(9090));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            eventLoopThread.start();
            return NullResponse.INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response postAcceptation(IOEvent.PostAcceptation postAcceptation) {
        try {
            final SocketChannel client = postAcceptation.socketChannel();
            SelectionKey sk = client.register(selector, SelectionKey.OP_READ);
            ActorRef connectionActor = context().fork(new ConnectionActor(new ClientContext(client, selectorExecutor, sk), protocolHandler, transport));
            connectionActors.put(sk, connectionActor);
            sk.attach(connectionActor);
            selector.wakeup();
            return NullResponse.INSTANCE;
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    private Response interestOps(IOEvent.InterestOps interestOps) {
        interestOps.key().interestOps(interestOps.ops());
        selector.wakeup();
        return NullResponse.INSTANCE;
    }

    private Response closeClient(IOEvent.Closed closed) {
        try {
            ActorRef connectionActor = connectionActors.get(closed.key());
            connectionActor.stop();
            connectionActors.remove(closed.key());
            closed.key().cancel();
            closed.key().channel().close();
            selector.wakeup();
        } catch (IOException ignored) {}
        return NullResponse.INSTANCE;
    }
}
