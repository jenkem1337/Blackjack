package org.Blackjack.application;

import org.Blackjack.domain.*;

public class BlackjackTable {

    public void shuffleDeckAndPutToShoe(Dealer dealer, Table table) {
        Deck shuffledDeck = dealer.shuffleDeck(table.getDeckFromShoe());
        table.putShuffledDeckToShoe(shuffledDeck);
    }

    public void dealCardToPlayer(Player player, Table table) {
        Card card = table.drawCardFromShoe();
        table.placeCardToPlayerSpot(player, card);
    }
}
