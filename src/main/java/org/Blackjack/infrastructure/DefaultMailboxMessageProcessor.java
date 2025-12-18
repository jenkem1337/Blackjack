package org.Blackjack.infrastructure;

import org.Blackjack.application.ApplicationException;
import org.Blackjack.domain.DomainException;

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
                    if(response instanceof NullResponse) {
                        continue;
                    }
                    msg.complete(response);
                } catch (DomainException | ApplicationException domainOrApplicationException) {
                    msg.complete(new ApplicationErrorResponse(domainOrApplicationException.getMessage()));
                } catch (Throwable throwable) {
                    msg.complete(new SystemErrorResponse(throwable.getMessage()));
                }
            }
        };
    }
}
