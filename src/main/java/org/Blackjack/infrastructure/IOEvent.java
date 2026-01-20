package org.Blackjack.infrastructure;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

sealed public interface IOEvent {
    record Start(int port) implements IOEvent {}
    record Acceptable(SelectionKey selectionKey) implements IOEvent {}
    record PostAcceptation(SocketChannel socketChannel) implements IOEvent {}
    record InterestOps(SelectionKey key ,int ops) implements IOEvent {}
    record Readable() implements IOEvent {}
    record Writable() implements IOEvent {}
    record Closed(SelectionKey key) implements IOEvent {}
}

