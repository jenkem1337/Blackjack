package org.Blackjack.application.response;

import org.Blackjack.application.GameRoomState;
import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.Response;

import java.util.UUID;

public record RoomCreatedAndRegistered(UUID uuid, PlayerID playerID, GameRoomState state)  {

}
