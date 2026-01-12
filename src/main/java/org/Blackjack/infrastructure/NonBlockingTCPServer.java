package org.Blackjack.infrastructure;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NonBlockingTCPServer {
    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private Set<SocketChannel> connections = ConcurrentHashMap.newKeySet();
    private SelectorExecutor selectorExecutor;
    private Transport transport;
    private ProtocolHandler protocolHandler;
    public NonBlockingTCPServer(Transport transport, ProtocolHandler protocolHandler, int port) {
        this.transport = transport;
        this.protocolHandler = protocolHandler;
        this.port = port;
    }

    public void start() {
        try {
            initServer();
            System.out.println("Server started on port " + port);

            while (true) {
                selector.select();
                selectorExecutor.executeTasks();
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

    private void initServer() throws IOException {
        selector = Selector.open();
        selectorExecutor = new SelectorExecutor(selector, new ConcurrentLinkedQueue<>());
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
        selectionKey.attach(new ClientContext(client, selectorExecutor, selectionKey));
        System.out.println("New client connected: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        ClientContext clientContext = (ClientContext) key.attachment();

        TransportReadResponse result = transport.read(clientContext);

        switch (result) {
            case TransportReadResponse.Data(ByteBuffer data) -> {
                protocolHandler.onData(clientContext, data);
                key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                selector.wakeup();
            }
            case TransportReadResponse.NeedWrite _ -> {
                key.interestOps(SelectionKey.OP_WRITE);
            }
            case TransportReadResponse.Closed _ -> {
                selectorExecutor.offerTask(() -> closeClient(key));
                selector.wakeup();
            }
        }

    }

    private void handleWrite(SelectionKey key) throws IOException {
        ClientContext ctx = (ClientContext) key.attachment();

        TransportWriteResponse result = transport.write(ctx);

        switch (result) {
            case TransportWriteResponse.Partial _ ->
                    key.interestOps(SelectionKey.OP_WRITE);
            case TransportWriteResponse.Done _ ->
                key.interestOps(SelectionKey.OP_READ);
            case TransportWriteResponse.Closed _ -> {
                selectorExecutor.offerTask(() -> closeClient(key));
                selector.wakeup();
            }
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
}