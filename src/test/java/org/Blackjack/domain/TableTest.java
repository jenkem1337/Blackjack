package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void shouldPutCardToDiscardTray_WhenCardHasPlayed() {
        var table = new Table();
        var usedCard = table.drawCardFromShoe();
        assertTrue(table.putUsedCardToDiscardTray(usedCard));
    }

    @Test
    void shouldReturnTrue_WhenPlayerJoinToTable() {
        var table = new Table();
        assertTrue(table.joinToTable(new Player()));
    }
    @Test
    void shouldReturnFalse_WhenPlayerAlreadyInTheTable() {
        var table = new Table();
        var player = new Player();
        table.joinToTable(player);
        assertFalse(table.joinToTable(player));
    }

    @Test
    void shouldReturnSpadesAce_WhenDrawCardFromShoeUnShuffledDeck() {
        var table = new Table();
        var card = table.drawCardFromShoe();
        assertEquals(Suit.SPADES, card.suit());
        assertEquals(Rank.ACE, card.rank());
    }

    @Test
    void shouldPlaceCardToPlayerBettingSpot_WhenCallingPlaceCardToPlayerSpot() {
        var table = new Table();
        var player = new Player();
        table.joinToTable(player);
        table.placeCardToPlayerSpot(player, Card.newInstanceWithFaceSide(Suit.CLUB, Rank.ACE));
        table.placeCardToPlayerSpot(player, Card.newInstanceWithFaceSide(Suit.CLUB, Rank.KING));
        assertEquals(21, table.playerSpot(player).handValue());
    }
    @Test
    void shouldPutShuffledDeckToShoe() {
        assertDoesNotThrow(() -> {
            var table = new Table();
            var dealer = new Dealer();
            var deck = dealer.shuffleDeck(table.getDeckFromShoe());
            table.putShuffledDeckToShoe(deck);
        });
    }
    @Test
    void shouldThrowIllegalStateException_WhenDeckIsNotShuffled() {
        assertThrows(IllegalStateException.class, () -> {
            var table = new Table();
            table.putShuffledDeckToShoe(table.getDeckFromShoe());
        });
    }
}