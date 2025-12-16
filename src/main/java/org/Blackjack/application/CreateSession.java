package org.Blackjack.application;

import org.Blackjack.domain.Player;
import org.Blackjack.infrastructure.Command;
import org.Blackjack.infrastructure.Response;

import java.util.concurrent.CompletableFuture;

public record CreateSession(Player player, CompletableFuture<Response> future) implements Command {
    @Override
    public void complete(Response responseMessage) {
        future.complete(responseMessage);
    }

    @Override
    public CompletableFuture<Response> future() {
        return future;
    }
}
