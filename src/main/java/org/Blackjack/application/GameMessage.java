package org.Blackjack.application;

import java.util.concurrent.CompletableFuture;

public interface GameMessage {
    void complete(GameMessage responseMessage);
    CompletableFuture<GameMessage> future();
    String message();
}
