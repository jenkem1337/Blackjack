package org.Blackjack.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActorSystemTest {
    ActorSystem actorSystem = new ActorSystem();

    @Test
    void forkActorRefWithDefaultValues() {
        ActorRef actorRef =  actorSystem.fork(new AbstractActor<Long>() {
            @Override
            public Response onReceive(Command command) {
                return null;
            }
        });
        assertNotNull(actorRef);
    }
}