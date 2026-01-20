package org.Blackjack.infrastructure;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ConnectionActor extends AbstractActor{
    private ClientContext clientContext;
    private ProtocolHandler protocolHandler;
    private Transport transport;

    public ConnectionActor (ClientContext clientContext, ProtocolHandler protocolHandler, Transport transport) {
        this.clientContext = clientContext;
        this.protocolHandler = protocolHandler;
        this.transport = transport;
    }
    @Override
    public Response onReceive(Command command) {
        return switch (command.message()) {
            case IOEvent.Readable readable -> read(readable);
            case IOEvent.Writable writable -> write(writable);
            default -> throw  new IllegalStateException();
        };
    }

    private Response read(IOEvent.Readable readable){
        TransportReadResponse result = transport.read(clientContext);

        switch (result) {
            case TransportReadResponse.Data(String data) -> {
                protocolHandler.onData(clientContext, data);
                context().sendMessageToParent(new IOEvent.InterestOps(clientContext.selectionKey(), SelectionKey.OP_WRITE | SelectionKey.OP_READ));
//                key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
//                selector.wakeup();
            }
            case TransportReadResponse.NeedWrite _ -> {
//                context().sendMessageToParent(new IOEvent.InterestOps(clientContext.selectionKey(), SelectionKey.OP_WRITE));
                    clientContext.selectionKey().interestOps(SelectionKey.OP_WRITE);
//                key.interestOps(SelectionKey.OP_WRITE);
            }
            case TransportReadResponse.Closed _ -> {
                context().sendMessageToParent(new IOEvent.Closed(clientContext.selectionKey()));

//                selectorExecutor.offerTask(() -> closeClient(key));
//                selector.wakeup();
            }
        }

        return NullResponse.INSTANCE;
    }
    private Response write(IOEvent.Writable writable){
        TransportWriteResponse result = transport.write(clientContext);

        switch (result) {
            case TransportWriteResponse.Partial _ ->
//                    context().sendMessageToParent(new IOEvent.InterestOps(clientContext.selectionKey(), SelectionKey.OP_WRITE));
            clientContext.selectionKey().interestOps(SelectionKey.OP_WRITE);

//            key.interestOps(SelectionKey.OP_WRITE);
            case TransportWriteResponse.Done _ ->
//                    context().sendMessageToParent(new IOEvent.InterestOps(clientContext.selectionKey(), SelectionKey.OP_READ));
                    clientContext.selectionKey().interestOps(SelectionKey.OP_READ);

//                    key.interestOps(SelectionKey.OP_READ);
            case TransportWriteResponse.Closed _ -> {
                context().sendMessageToParent(new IOEvent.Closed(clientContext.selectionKey()));

//                selectorExecutor.offerTask(() -> closeClient(key));
//                selector.wakeup();
            }
        }

        return NullResponse.INSTANCE;

    }
}
