package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpotTest {
    @Test
    void placeCard() {
        var spot = Spot.forPlayer(new PlayerID());
        var response = spot.placeCard(Card.newInstanceWithFaceSide(Suit.CLUB, Rank.FIVE));
        var secondResponse = spot.placeCard(Card.newInstanceWithFaceSide(Suit.DIAMOND, Rank.SEVEN));
        assertTrue(response);
        assertTrue(secondResponse);
        assertEquals(12, spot.handValue());
    }
    @Test
    void shouldReturnBlackjack() {
        var spot = Spot.forPlayer(new PlayerID());
        spot.placeCard(Card.newInstanceWithFaceSide(Suit.CLUB, Rank.ACE));
        spot.placeCard(Card.newInstanceWithFaceSide(Suit.DIAMOND, Rank.KING));
        assertEquals(21, spot.handValue());
    }
    @Test
    void shouldHandValueIsEqualTo16_WhenTwoAceAndFive() {
        var spot = Spot.forPlayer(new PlayerID());
        spot.placeCard(Card.newInstanceWithFaceSide(Suit.CLUB, Rank.ACE));
        spot.placeCard(Card.newInstanceWithFaceSide(Suit.DIAMOND, Rank.ACE));
        spot.placeCard(Card.newInstanceWithFaceSide(Suit.DIAMOND, Rank.FOUR));
        assertEquals(16, spot.handValue());
    }

    @Test
    void shouldThrowIllegalStateException_WhenHandValueBiggerThan21() {
        assertThrows(DomainException.class, () -> {
            var spot = Spot.forPlayer(new PlayerID());
            spot.placeCard(Card.newInstanceWithFaceSide(Suit.DIAMOND, Rank.KING));
            spot.placeCard(Card.newInstanceWithFaceSide(Suit.SPADES, Rank.KING));
            spot.placeCard(Card.newInstanceWithFaceSide(Suit.CLUB, Rank.TWO));
        });
    }
}