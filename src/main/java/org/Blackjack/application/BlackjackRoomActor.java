package org.Blackjack.application;

import org.Blackjack.domain.Dealer;
import org.Blackjack.domain.Player;
import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.AbstractActor;
import org.Blackjack.infrastructure.Command;
import org.Blackjack.infrastructure.Response;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public final class BlackjackRoomActor extends AbstractActor<RoomId> {
    private final Dealer dealer;
    private final BlackjackTable table;
    private final Map<PlayerID, Player> players;
    private PlayerID roomOwnerId;
    private GameRoomState gameRoomState;

    public BlackjackRoomActor() {
        this.dealer  = new Dealer();
        this.table   = new BlackjackTable();
        this.players = new HashMap<>();
        this.gameRoomState = GameRoomState.ROOM_CREATED;
    }

    @Override
    public Response onReceive(Command message) {
        return switch (message) {
            case CreateSession createSession -> onCreateSessionCommand(createSession);
            default -> throw new IllegalStateException("Unexpected value: " + message);
        };
    }

    private SessionCreated onCreateSessionCommand(CreateSession command) {

        if(gameRoomState != GameRoomState.ROOM_CREATED) {
            throw new IllegalStateException("Room state must be ROOM_CREATED");
        }

        roomOwnerId = command.player().id();

        players.putIfAbsent(command.player().id(), command.player());

        gameRoomState = GameRoomState.WAITING_PLAYERS;

        return new SessionCreated(roomOwnerId, id() , gameRoomState);
    }

}
