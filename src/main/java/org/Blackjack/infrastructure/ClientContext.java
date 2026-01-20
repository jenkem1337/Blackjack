package org.Blackjack.infrastructure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class ClientContext {
    public final SocketChannel channel;
    public final ByteBuffer readBuffer  = ByteBuffer.allocate(4096);
    public final Queue<ByteBuffer> writeQueue = new ArrayDeque<>();
    private final SelectorExecutor selectorExecutor;
    private final SelectionKey key;

    ClientContext(SocketChannel channel, SelectorExecutor selectorExecutor, SelectionKey key) {
        this.channel = channel;
        this.selectorExecutor = selectorExecutor;
        this.key = key;
    }
    public SelectionKey selectionKey() {return key;}
    public void write(ByteBuffer buffer) {
        selectorExecutor.offerTask(() -> {
            writeQueue.add(buffer);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        });
    }
}
