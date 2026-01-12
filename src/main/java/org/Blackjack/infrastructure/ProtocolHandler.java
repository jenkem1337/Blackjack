package org.Blackjack.infrastructure;

import java.nio.ByteBuffer;

public interface ProtocolHandler {
    void onData(ClientContext ctx, ByteBuffer data);
}
