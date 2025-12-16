package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ShoeTest {

    @Test
    void shouldCreateNewShoeInstance() {
        var shoe = Shoe.empty();
        assertNotNull(shoe);
    }
    @Test
    void shouldThrowIllegalStateExceptionCallingPutShuffledDeck_WhenDeckIsNotShuffled(){
        assertThrows(IllegalStateException.class, () -> {
            var shoe = Shoe.empty();
            shoe.putShuffledDeck(Deck.withUnShuffledCards());
        });
    }

    @Test
    void shouldPutShuffledDeckToShoe_WhenCallingPutShuffledDeck() {
        var shoe = Shoe.createWithUnShuffledDeck();
        var shuffledDeck = Deck.withShuffledDeck(shoe.deck().cards());
        assertDoesNotThrow(() -> shoe.putShuffledDeck(shuffledDeck));
    }

    @Test
    void shouldDrawCard_WhenUnShuffledDeck() {
        var shoe = Shoe.createWithUnShuffledDeck();
        var card = shoe.drawCard();

        assertEquals(Suit.SPADES, card.suit());
        assertEquals(Rank.ACE, card.rank());
    }
}