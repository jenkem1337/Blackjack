package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractActor<ID> implements Actor<Command, Response>{
    private ID id;
    private Queue<Command> selfQueue;

    void setSelfQueue(Queue<Command> queue) {
        if(queue == null) throw new IllegalArgumentException("Queue must not be null !");
        this.selfQueue = queue;
    }
    public void selfEnqueue(CompletableFuture<Command> future) {
    }

    public ID id () {return id;}
}
