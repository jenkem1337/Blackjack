package org.Blackjack.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {
    private final PlayerID id;
    private String username;
    private boolean isInGame;

    public Player(PlayerID id, String username, boolean isInGame) {
        this.id = id;
        this.username = username;
        this.isInGame = isInGame;
    }
    public Player() {
        this(new PlayerID(), null, false);
    }
    public PlayerID id() {return id;}
    public String username(){return username;}
}
