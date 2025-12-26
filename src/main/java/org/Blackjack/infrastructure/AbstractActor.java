package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractActor implements Actor<Command, Response>{
    private final UUID id = UUID.randomUUID();
    private Queue<Command> selfQueue;
    private ActorContext context;

    void setSelfQueue(Queue<Command> queue) {
        if(queue == null) throw new IllegalArgumentException("Queue must not be null !");
        this.selfQueue = queue;
    }
    public void selfEnqueue(CompletableFuture<Response> future) {
        future.whenComplete(((response, throwable) -> {
                if(throwable != null) {
                    selfQueue.offer(new AsyncCommand(new InternalErrorCommand(throwable.getMessage())));
                    return;
                }
                selfQueue.offer(new AsyncCommand(response));
        }));
    }
    public void  selfEnqueueWithExternalFuture(
            CompletableFuture<Response> future,
            CompletableFuture<?> externalFuture,
            Function<Response,?> responseToCommandMapper) {
        future.whenComplete(((response, throwable) -> {
            if(throwable != null) {
                selfQueue.offer(new AsyncCommand(new InternalErrorCommand(throwable.getMessage()), externalFuture));
                return;
            }
            var command = responseToCommandMapper.apply(response);
            selfQueue.offer(new AsyncCommand(command, externalFuture));
        }));

    }
    void attachContext(ActorContext context) {
        this.context = context;
    }

    protected ActorContext context() {
        return context;
    }
    public UUID id () {return id;}
}
