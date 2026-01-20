package org.Blackjack.infrastructure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class PlainTextTransport implements  Transport{


    @Override
    public TransportReadResponse read(ClientContext ctx) {
        try {
            ByteBuffer buffer = ctx.readBuffer;
            int read = ctx.channel.read(buffer);
            if (read == -1) {
                return new TransportReadResponse.Closed();
            }

            if (read == 0) {
                return new TransportReadResponse.NeedWrite();
            }

            buffer.flip();
            ByteBuffer data = ByteBuffer.allocate(buffer.remaining());
            data.put(buffer);
            data.flip();
            buffer.clear();
            String message = new String(data.array(), 0, data.remaining()).trim();

            if(message.startsWith("EXIT")) {
                return new TransportReadResponse.Closed();
            }

            return new TransportReadResponse.Data(message);

        } catch (IOException ioException) {
            return null;
        }

    }

    @Override
    public TransportWriteResponse write(ClientContext ctx) {
        try{
            ByteBuffer buffer = ctx.writeQueue.peek();

            if (buffer == null) {
                return new TransportWriteResponse.Done();
            }

            ctx.channel.write(buffer);

            if (buffer.hasRemaining()) {
                return new TransportWriteResponse.Partial();
            }

            ctx.writeQueue.poll();
            return new TransportWriteResponse.Done();

        } catch (IOException ioException) {
            return null;
        }
    }
}
