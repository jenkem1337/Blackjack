package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public interface ActorRef {

    CompletableFuture<Response> send(Command msg);

    void start();
    void stop();
}
