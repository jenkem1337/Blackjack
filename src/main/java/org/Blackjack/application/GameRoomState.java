package org.Blackjack.application;

public enum GameRoomState {
    ROOM_CREATED,
    WAITING_PLAYERS,
    DEALING_INITIAL_CARDS,
    PLAYER_TURN,
    DEALER_TURN,
    EVALUATE_RESULTS,
    ROUND_FINISHED,
    ROOM_CLOSED;
}
