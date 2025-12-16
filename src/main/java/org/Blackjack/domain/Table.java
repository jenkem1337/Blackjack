package org.Blackjack.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Table {
    private final Shoe shoe;
    private final DiscardTray discardTray;
    private final Spot dealerSpot;
    private final Map<PlayerID, Spot> bettingSpots;
    public Table() {
        this.shoe = Shoe.createWithUnShuffledDeck();
        this.discardTray = DiscardTray.empty();
        this.dealerSpot = Spot.forDealer();
        this.bettingSpots = new HashMap<>();
    }

    public boolean putUsedCardToDiscardTray(Card usedCard) {
        return discardTray.addUsedCard(usedCard);
    }

    public void placeCardToPlayerSpot(Player player, Card card) {
        if(!bettingSpots.containsKey(player.id())) return;
        var spot = bettingSpots.get(player.id());
        spot.placeCard(card);
    }
    public boolean joinToTable(Player player) {
        if(bettingSpots.containsKey(player.id())) {
            return false;
        }
        bettingSpots.putIfAbsent(player.id(), Spot.forPlayer(player.id()));
        return true;
    }
    public Card drawCardFromShoe() {
        return shoe.drawCard();
    }
    public Deck getDeckFromShoe(){
        return shoe.deck();
    }
    public void putShuffledDeckToShoe(Deck shuffledDeck){
        shoe.putShuffledDeck(shuffledDeck);
    }
    public DiscardTray discardTray() {return discardTray;}
    public Map<PlayerID, Spot> spots(){return bettingSpots;}
    public Spot playerSpot(Player player) {return bettingSpots.get(player.id());}
}
