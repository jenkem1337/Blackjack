package org.Blackjack.domain;

import java.util.UUID;

public class PlayerID {
    private final UUID id;
    public PlayerID (UUID id) {
        this.id = id;
    }
    public PlayerID() {
        this(UUID.randomUUID());
    }
    public UUID id() {
        return id;
    }
}
