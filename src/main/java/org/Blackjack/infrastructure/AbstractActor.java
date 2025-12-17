package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractActor implements Actor<Command, Response>{
    private final UUID id = UUID.randomUUID();
    private Queue<Command> selfQueue;
    private ActorContext context;

    void setSelfQueue(Queue<Command> queue) {
        if(queue == null) throw new IllegalArgumentException("Queue must not be null !");
        this.selfQueue = queue;
    }
    public void selfEnqueue(CompletableFuture<Command> future) {
    }
    void attachContext(ActorContext context) {
        this.context = context;
    }

    protected ActorContext context() {
        return context;
    }
    public UUID id () {return id;}
}
