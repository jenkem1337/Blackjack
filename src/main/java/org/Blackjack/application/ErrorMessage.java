package org.Blackjack.application;

import java.util.concurrent.CompletableFuture;

public record ErrorMessage(String message) implements GameMessage {
    @Override
    public void complete(GameMessage responseMessage) {

    }

    @Override
    public CompletableFuture<GameMessage> future() {
        return null;
    }
}
