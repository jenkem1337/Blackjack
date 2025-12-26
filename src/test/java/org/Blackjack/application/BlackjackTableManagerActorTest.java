package org.Blackjack.application;

import org.Blackjack.application.command.CreateRoom;
import org.Blackjack.application.response.RoomCreatedAndRegistered;
import org.Blackjack.domain.Player;
import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class BlackjackTableManagerActorTest {

    static ActorSystem system;
    ActorRef managerRef;
    @BeforeAll
    static void setUp() {
        system = new ActorSystem();
    }

    @Test
    void sendMessage() {
        managerRef = system.fork(new BlackjackTableManagerActor());
        Player player = new Player();
        PlayerID playerID = player.id();
        CompletableFuture<Response> futureResponse = managerRef.send(new AsyncCommand(new CreateRoom(player)));
        RoomCreatedAndRegistered roomCreatedAndRegistered = ((SuccessResponse<RoomCreatedAndRegistered>)futureResponse.join()).message();
        assertInstanceOf(RoomCreatedAndRegistered.class, roomCreatedAndRegistered);
        assertEquals(playerID, roomCreatedAndRegistered.playerID());
        assertEquals(GameRoomState.WAITING_PLAYERS, roomCreatedAndRegistered.state());
    }
}