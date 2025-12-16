package org.Blackjack.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {
    private final PlayerID id;
    private String nickName;
    private boolean isInGame;

    public Player(PlayerID id) {
        this.id = id;
    }
    public Player() {
        this(new PlayerID());
    }
    public PlayerID id() {return id;}
}
