package org.Blackjack.application;

import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.Response;

import java.util.UUID;

public record SessionCreated(PlayerID roomOwnerId, UUID roomId, GameRoomState gameRoomState) implements Response {
}
