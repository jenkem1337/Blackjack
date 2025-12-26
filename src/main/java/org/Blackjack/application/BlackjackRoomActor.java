package org.Blackjack.application;

import org.Blackjack.application.command.CreateRoom;
import org.Blackjack.application.response.RoomSessionCreatedResponse;
import org.Blackjack.domain.Dealer;
import org.Blackjack.domain.Player;
import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.AbstractActor;
import org.Blackjack.infrastructure.Command;
import org.Blackjack.infrastructure.Response;
import org.Blackjack.infrastructure.SuccessResponse;

import java.util.HashMap;
import java.util.Map;

public final class BlackjackRoomActor extends AbstractActor {


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
    public Response onReceive(Command command) {
        return switch (command.message()) {
            case CreateRoom createRoom -> onCreateSessionCommand(createRoom);
            default -> throw new IllegalStateException("Unexpected value: " + command.message());
        };
    }

    private Response onCreateSessionCommand(CreateRoom command) {

        if(gameRoomState != GameRoomState.ROOM_CREATED) {
            throw new IllegalStateException("Room state must be ROOM_CREATED");
        }

        roomOwnerId = command.player().id();

        players.putIfAbsent(command.player().id(), command.player());

        gameRoomState = GameRoomState.WAITING_PLAYERS;
        return new SuccessResponse<>(new RoomSessionCreatedResponse(roomOwnerId, id() , gameRoomState));
    }

}
