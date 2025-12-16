package org.Blackjack.domain;

import java.util.ArrayList;
import java.util.List;

public class Spot {
    private final PlayerID playerID;
    private final List<Card> playerHands = new ArrayList<>();
    private long handValue;

    private Spot(PlayerID playerId) {
        this.playerID = playerId;
    }

    public static Spot forPlayer(PlayerID id) {
        return new Spot(id);
    }
    public static Spot forDealer() {
        return new Spot(null);
    }

    public PlayerID playerId() {return playerID;}
    public List<Card> playerHands() {return playerHands;}

    public long handValue() {return handValue;}

    public boolean placeCard(Card card) {
        playerHands.add(card);
        if(isCardRankEqualToAce(card) && isHandValueBiggerThan21WhenCardIsAce(card)) {
            if(incrementHandValueByOneAndCheckBiggerThan21(card)) {
                throw new IllegalStateException("Hand bigger than 21 !");
            }
            return true;
        }
        if(incrementHandValueByRankValueAndCheckBiggerThan21(card)) {
            throw new IllegalStateException("Hand bigger than 21 !");
        }
        return true;
    }

    private boolean isCardRankEqualToAce(Card card) {
        return card.rank() == Rank.ACE;
    }
    private boolean isHandValueBiggerThan21WhenCardIsAce(Card card) {
        if(card.rank() != Rank.ACE) return false;
        return (handValue + card.rankValue()) > 21;
    }
    private boolean incrementHandValueByRankValueAndCheckBiggerThan21(Card card){
        handValue += card.rankValue();
        return handValue > 21;
    }
    private boolean incrementHandValueByOneAndCheckBiggerThan21(Card card) {
        handValue += 1;
        return handValue > 21;
    }
}
