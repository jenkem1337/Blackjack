package org.Blackjack.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface Command {
    void complete(Response responseMessage);
    CompletableFuture<Response> future();
}
