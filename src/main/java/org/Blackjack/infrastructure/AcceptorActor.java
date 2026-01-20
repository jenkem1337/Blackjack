package org.Blackjack.infrastructure;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptorActor extends AbstractActor{

    public AcceptorActor() {
    }

    @Override
    public Response onReceive(Command command) {
        return switch (command.message()) {
            case IOEvent.Acceptable acceptable -> accept(acceptable);
            default -> throw new IllegalStateException("Unexpected value: " + command.message());
        };
    }

    private Response accept(IOEvent.Acceptable acceptable) {
        try {
            final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) acceptable.selectionKey().channel();
            final SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            context().sendMessageToParent(new IOEvent.PostAcceptation(socketChannel));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return NullResponse.INSTANCE;
    }
}
