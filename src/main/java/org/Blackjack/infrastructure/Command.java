package org.Blackjack.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface Command<T> {
    void complete(Response responseMessage);
    CompletableFuture<Response> future();
    T command();
}
