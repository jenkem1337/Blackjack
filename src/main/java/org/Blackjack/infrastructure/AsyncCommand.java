package org.Blackjack.infrastructure;

import java.util.concurrent.CompletableFuture;

public record AsyncCommand<T>(T message, CompletableFuture<Response> future) implements Command<T> {
    public AsyncCommand(T message){
        this(message, new CompletableFuture<>());
    }

    @Override
    public void complete(Response responseMessage) {
        future.complete(responseMessage);
    }
}
