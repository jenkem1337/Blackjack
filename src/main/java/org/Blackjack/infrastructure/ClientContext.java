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

    public void writeLater(ByteBuffer buffer) {
        selectorExecutor.offerTask(() -> {
            writeQueue.add(buffer);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        });
    }

    public void closeLater() {
        selectorExecutor.offerTask(() -> {
            try {
                key.cancel();
                channel.close();
            } catch (IOException ignored) {}
        });
    }

}
