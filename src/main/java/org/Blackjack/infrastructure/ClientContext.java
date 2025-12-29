package org.Blackjack.infrastructure;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class ClientContext {
    final SocketChannel channel;
    final Queue<ByteBuffer> writeQueue = new ArrayDeque<>();

    ClientContext(SocketChannel channel) {
        this.channel = channel;
    }
}
