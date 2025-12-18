package org.Blackjack.infrastructure;

import java.util.concurrent.CompletableFuture;

public record AsyncCommand<T>(T command, CompletableFuture<Response> future) implements Command<T> {
    public AsyncCommand(T command){
        this(command, new CompletableFuture<>());
    }

    @Override
    public void complete(Response responseMessage) {
        future.complete(responseMessage);
    }
}
