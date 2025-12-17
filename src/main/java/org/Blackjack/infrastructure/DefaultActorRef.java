package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class DefaultActorRef implements ActorRef {
    private final ActorCell actorCell;
    DefaultActorRef(ActorCell actorCell) {
        this.actorCell = actorCell;
    }

    @Override
    public CompletableFuture<Response> send(Command msg) {
        actorCell.enqueue(msg);
        return msg.future();
    }

    @Override
    public void start() {
        actorCell.start();
    }

    @Override
    public void stop() {
        actorCell.stop();
    }

}
