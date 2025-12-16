package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void shouldReturnFaceSideCardInstance_WhenCreateCardWithNewInstanceWithFaceSide() {
        var card = Card.newInstanceWithFaceSide(Suit.CLUB, Rank.KING);
        assertNotNull(card);
        assertEquals(Rank.KING, card.rank());
        assertEquals(Suit.CLUB, card.suit());
    }

    @Test
    void shouldReturnBackSideCard_WhenCreateCardWithNewInstanceWithBackSide() {
        var card = Card.newInstanceWithBackSide(Suit.CLUB, Rank.KING);
        assertNotNull(card);
        assertEquals(Rank.SECRET, card.rank());
        assertEquals(Suit.SECRET, card.suit());
    }

    @Test
    void shouldFlipCardToBackSideToFaceSide_WhenCardFaceIsEqualToBackSide() {
        var card = Card.newInstanceWithBackSide(Suit.CLUB, Rank.KING);

        assertEquals(Rank.SECRET, card.rank());
        assertEquals(Suit.SECRET, card.suit());

        card.flipCardFaceToFace();

        assertEquals(Rank.KING, card.rank());
        assertEquals(Suit.CLUB, card.suit());
    }

    @Test
    void shouldFlipCardFaceToFaceSideToBackSide_WhenCardFaceIsEqualToFaceSide() {
        var card = Card.newInstanceWithFaceSide(Suit.CLUB, Rank.KING);

        assertEquals(Rank.KING, card.rank());
        assertEquals(Suit.CLUB, card.suit());

        card.flipCardFaceToBack();

        assertEquals(Rank.SECRET, card.rank());
        assertEquals(Suit.SECRET, card.suit());

    }
}