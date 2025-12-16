package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void shouldReturnNewUnShuffledDeck_WhenCallingWithUnShuffledCards() {
        var deck = Deck.withUnShuffledCards();
        assertNotNull(deck);
        assertEquals(DeckState.UNSHUFFLED, deck.state());
    }

    @Test
    void shouldReturnFiftyTwo_WhenQueryDeckArraySize() {
        var deck = Deck.withUnShuffledCards();
        var deckList = deck.cards();
        assertEquals(52, deckList.size());
    }

    @Test
    void shouldReturnFalse_WhenCallingIsShuffled() {
        var deck = Deck.withUnShuffledCards();
        assertFalse(deck.isShuffled());
    }
}