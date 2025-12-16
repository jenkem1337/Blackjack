package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

interface ActorRef {

    CompletableFuture<Response> send(Command msg);

    static ActorRef createDefaultActorRefAndStartImmediately(AbstractActor<?> actor, Queue<Command> queue, WaitStrategy waitStrategy, MessageProcessorFunction mailboxThreadExecutorCallback) {
        actor.setSelfQueue(queue);
        var mailbox =  new DefaultActorRef(actor, queue, waitStrategy, mailboxThreadExecutorCallback);
        mailbox.start();
        return mailbox;
    }
    static ActorRef createDefaultActorRef(AbstractActor<?> actor, Queue<Command> mailbox, WaitStrategy waitStrategy, MessageProcessorFunction mailboxThreadExecutorCallback) {
        actor.setSelfQueue(mailbox);
        return new DefaultActorRef(actor, mailbox, waitStrategy, mailboxThreadExecutorCallback);
    }
    void start();
    void stop();
}
