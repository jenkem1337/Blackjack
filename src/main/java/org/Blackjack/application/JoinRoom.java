package org.Blackjack.application;

import java.util.concurrent.CompletableFuture;

public record JoinRoom(String message, CompletableFuture<GameMessage> future) implements GameMessage {
    @Override
    public void complete(GameMessage responseMessage) {
        future.complete(responseMessage);
    }

    @Override
    public CompletableFuture<GameMessage> future() {
        return future;
    }
}
