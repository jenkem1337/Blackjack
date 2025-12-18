package org.Blackjack.application.response;

import org.Blackjack.application.GameRoomState;
import org.Blackjack.domain.PlayerID;

import java.util.UUID;

public record RoomSessionCreatedResponse(PlayerID roomOwner, UUID uuid, GameRoomState state){}

