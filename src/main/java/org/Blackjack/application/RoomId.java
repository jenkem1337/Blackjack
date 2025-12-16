package org.Blackjack.application;

import java.util.UUID;

public class RoomId {
    public final UUID id;

    public RoomId(UUID id) {
        this.id = id;
    }
    public RoomId() {
        this(UUID.randomUUID());
    }

    public UUID id() {
        return id;
    }

    public String idAsString() {return id.toString();}
}
