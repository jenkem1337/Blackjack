package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActorSystem {
//    private final ActorRouter router;

    public ActorRef fork(AbstractActor<?> actor, Queue<Command> mailbox, WaitStrategy waitStrategy, MessageProcessorFunction callback) {

        return ActorRef.createDefaultActorRef(actor, mailbox, waitStrategy, callback);
    }

    public ActorRef fork(AbstractActor<?> actor) {
        return ActorRef.createDefaultActorRef(actor, new ConcurrentLinkedQueue<>(), new ThreadYieldWaitStrategy(), new DefaultMailboxMessageProcessor());
    }
}
