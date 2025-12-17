package org.Blackjack.infrastructure;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActorSystem {
    private final Map<ActorRef, ActorCell> registry = new ConcurrentHashMap<>();


    public ActorRef fork(AbstractActor actor, ActorCell parent) {
        Queue<Command> mailbox = new ConcurrentLinkedQueue<>();
        ActorCell cell = new ActorCell(
                this,
                actor,
                mailbox,
                new ThreadYieldWaitStrategy(),
                new DefaultMailboxMessageProcessor(),
                parent
        );

        ActorRef ref = new DefaultActorRef(cell);
        registry.put(ref, cell);
        if(parent != null) {
            parent.addChild(cell);
        }
        cell.start();
        return ref;
    }

    public ActorRef fork(AbstractActor actor) {
        return fork(actor, null);
    }
    public void stop(ActorRef actorRef) {
        ActorCell cell = registry.remove(actorRef);
        if (cell != null) {
            cell.stop();
        }

    }
}
