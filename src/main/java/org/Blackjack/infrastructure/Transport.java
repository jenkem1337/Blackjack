package org.Blackjack.infrastructure;

import java.nio.channels.SelectionKey;

public interface Transport{
    TransportReadResponse read(ClientContext ctx);
    TransportWriteResponse write(ClientContext ctx);
}
