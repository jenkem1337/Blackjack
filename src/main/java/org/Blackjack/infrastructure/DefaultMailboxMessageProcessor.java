package org.Blackjack.infrastructure;

import java.util.Queue;

public class DefaultMailboxMessageProcessor implements MessageProcessorFunction{
    @Override
    public Runnable apply(boolean isRunning, Actor<Command, Response> room, WaitStrategy waitStrategy, Queue<Command> mailbox) {
        return () -> {
            while(isRunning) {
                Command msg = mailbox.poll();
                if(msg == null) {
                    waitStrategy.onWait();
                    continue;
                }
                try {
                    Response response = room.onReceive(msg);
                    msg.complete(response);
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                    msg.complete(new ErrorResponse(e.getMessage()));
                }
            }
        };
    }
}
