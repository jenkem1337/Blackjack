package org.Blackjack.infrastructure;

public sealed interface TransportWriteResponse permits
        TransportWriteResponse.Done,
        TransportWriteResponse.Partial,
        TransportWriteResponse.Closed{
    record Done() implements TransportWriteResponse {}
    record Partial() implements TransportWriteResponse {}
    record Closed() implements TransportWriteResponse {}

}
