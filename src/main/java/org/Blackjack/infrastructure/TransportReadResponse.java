package org.Blackjack.infrastructure;

import java.nio.ByteBuffer;

public sealed interface TransportReadResponse permits
        TransportReadResponse.Data,
        TransportReadResponse.NeedWrite,
        TransportReadResponse.Closed{
    record Data(ByteBuffer buffer) implements TransportReadResponse {}
    record NeedWrite() implements TransportReadResponse {}
    record Closed() implements TransportReadResponse {}
}
