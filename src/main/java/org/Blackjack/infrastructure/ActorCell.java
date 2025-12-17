package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ActorCell {
    private final AbstractActor<?> actor;
    private final Queue<Command> mailbox;
    private final WaitStrategy waitStrategy;
    private final MessageProcessorFunction processorFactory;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread mailboxThread;

    ActorCell(
            AbstractActor<?> actor,
            Queue<Command> mailbox,
            WaitStrategy waitStrategy,
            MessageProcessorFunction processorFactory
    ) {
        this.actor = actor;
        this.mailbox = mailbox;
        this.waitStrategy = waitStrategy;
        this.processorFactory = processorFactory;

        actor.setSelfQueue(mailbox);
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

}
