package org.Blackjack.application;

import org.Blackjack.infrastructure.AbstractSupervisorActor;
import org.Blackjack.infrastructure.Command;
import org.Blackjack.infrastructure.Response;

public class BlackjackTableManagerActor extends AbstractSupervisorActor<Void> {
    @Override
    public Response onReceive(Command command) {
        return null;
    }
}
