package org.Blackjack.infrastructure;

import java.util.Queue;

@FunctionalInterface
public interface MessageProcessorFunction {
    Runnable apply(final boolean isRunning, final Actor<Command, Response> room, final WaitStrategy waitStrategy, final Queue<Command> mailbox);
}
