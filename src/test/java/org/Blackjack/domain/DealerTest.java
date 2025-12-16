package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DealerTest {
    @Test
    void shuffleDeck() {
        var dealer = new Dealer();
        Deck shuffledDeck = dealer.shuffleDeck(Deck.withUnShuffledCards());
        assertTrue(shuffledDeck.isShuffled());
        assertEquals(52, shuffledDeck.cards().size());
    }

}