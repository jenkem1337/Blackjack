package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

interface ActorRef {

    CompletableFuture<Response> send(Command msg);

    void start();
    void stop();
}
