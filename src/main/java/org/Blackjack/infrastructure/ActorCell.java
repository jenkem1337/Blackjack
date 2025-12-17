package org.Blackjack.infrastructure;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ActorCell {
    private final AbstractActor actor;
    private final Queue<Command> mailbox;
    private final WaitStrategy waitStrategy;
    private final MessageProcessorFunction processorFactory;

    private final ActorCell parent;
    private final Map<String, ActorCell> children = new ConcurrentHashMap<>();
    private final ActorContext context;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread mailboxThread;

    ActorCell(
            ActorSystem system,
            AbstractActor actor,
            Queue<Command> mailbox,
            WaitStrategy waitStrategy,
            MessageProcessorFunction processorFactory,
            ActorCell parent
    ) {
        this.actor = actor;
        this.mailbox = mailbox;
        this.waitStrategy = waitStrategy;
        this.processorFactory = processorFactory;
        this.parent = parent;
        this.context = new ActorContext(system, this);
        actor.setSelfQueue(mailbox);
        actor.attachContext(context);
    }

    void start() {
        if (running.compareAndSet(false, true)) {
            Runnable loop = processorFactory.apply(
                    true,
                    actor,
                    waitStrategy,
                    mailbox
            );

            mailboxThread = Thread.ofVirtual().unstarted(loop);
            mailboxThread.start();
        }
    }

    void stop() {
        if (running.compareAndSet(true, false)) {
            if (mailboxThread != null) {
                mailboxThread.interrupt();
            }
        }
    }

    void enqueue(Command msg) {
        mailbox.offer(msg);
    }

    boolean isRunning() {
        return running.get();
    }
    void addChild(ActorCell child) {
        children.putIfAbsent(child.actor.id().toString(), child);
    }

    void removeChild(ActorCell child) {
        children.remove(child.actor.id().toString());
    }

    Map<String, ActorCell> children() {
        return children;
    }

    ActorCell parent() {
        return parent;
    }
    public ActorContext context() {
        return context;
    }



}
