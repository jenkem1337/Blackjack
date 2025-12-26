package org.Blackjack.application.command;

import org.Blackjack.application.GameRoomState;
import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.ActorRef;

import java.util.UUID;

public record RegisterRoomToRegistry(UUID uuid, PlayerID roomOwner, GameRoomState state, ActorRef actorRef) {}

