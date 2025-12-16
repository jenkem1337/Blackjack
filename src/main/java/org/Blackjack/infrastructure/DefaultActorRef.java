package org.Blackjack.infrastructure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class DefaultActorRef implements ActorRef {
    private final Actor<Command, Response> actor;
    private final Thread mailboxThread;
    private final Queue<Command> mailbox;
    private final WaitStrategy waitStrategy;
    private volatile boolean isRunning;
    DefaultActorRef(Actor<Command, Response> actor, Queue<Command> mailbox, WaitStrategy waitStrategy, MessageProcessorFunction messageProcessingCallback) {
        this.actor = actor;
        this.isRunning = true;
        this.waitStrategy = waitStrategy;
        this.mailbox = mailbox;
        this.mailboxThread = Thread.ofVirtual()
                .unstarted(
                        messageProcessingCallback
                                .apply(
                                        this.isRunning,
                                        this.actor,
                                        this.waitStrategy,
                                        this.mailbox
                                )
                );
    }
    @Override
    public CompletableFuture<Response> send(Command msg) {
        mailbox.offer(msg);
        return msg.future();
    }

    private void processMessages() {
        while(isRunning && Thread.currentThread().isInterrupted()) {
            Command msg = mailbox.poll();
            if(msg == null) {
                waitStrategy.onWait();
                continue;
            }
            Response response = actor.onReceive(msg);
            msg.complete(response);
        }
    }

    @Override
    public void start() {
        isRunning = true;
        if(!mailboxThread.isAlive()){
            mailboxThread.start();
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        if(!mailboxThread.isInterrupted()){
            mailboxThread.interrupt();
        }
    }
}
