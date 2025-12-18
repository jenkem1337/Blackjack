package org.Blackjack.infrastructure;

public final class NullResponse implements Response {
    public static final NullResponse INSTANCE = new NullResponse();
    private NullResponse(){}
}
