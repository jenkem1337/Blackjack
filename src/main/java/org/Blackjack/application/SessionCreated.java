package org.Blackjack.application;

import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.Response;

public record SessionCreated(PlayerID roomOwnerId, RoomId roomId, GameRoomState gameRoomState) implements Response {
}
